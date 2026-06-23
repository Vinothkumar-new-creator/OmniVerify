from fastapi import FastAPI, UploadFile, File, Body, Header, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import requests
from PIL import Image
import io
import os
import re
import json

app = FastAPI()

# Enable CORS so your Android app can communicate smoothly with Render
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"]
)

# --- CONFIGURATION (Securely loading from Render Environment Variables) ---
SIGHTENGINE_USER = os.environ.get("SIGHTENGINE_USER")
SIGHTENGINE_SECRET = os.environ.get("SIGHTENGINE_SECRET")
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY")       # Reused for Text, Scripts, Links, and QR

# Shared secret the Android app sends on every request (see NetworkClient.kt).
# Set this in Render's environment variables to the SAME value you put in the
# app. While this var is unset, the check below is skipped (fail-open) so you
# can deploy this backend update before shipping the matching app update.
APP_SHARED_SECRET = os.environ.get("APP_SHARED_SECRET")

# How long we'll wait on each upstream API before giving up and telling the
# app "try again", instead of hanging until OkHttp's own timeout kicks in.
UPSTREAM_TIMEOUT_SECONDS = 20


def verify_app_key(x_omniverify_key: str = Header(default=None)):
    """
    Lightweight abuse deterrent: rejects requests that don't carry the app's
    shared secret header. NOTE: this is not strong security -- the secret
    lives in the APK and can be extracted by anyone who decompiles it. It
    raises the bar against casual scraping/quota abuse; it is not a
    substitute for per-user authentication.
    """
    if APP_SHARED_SECRET and x_omniverify_key != APP_SHARED_SECRET:
        raise HTTPException(status_code=401, detail="Missing or invalid app key")


# --- HEALTH CHECK / WARM-UP ENDPOINT ---
# The app pings this when the floating assistant turns on, so a sleeping
# Render free-tier instance has time to wake up *before* you crop an image,
# instead of waking up *during* your scan request.
@app.get("/")
@app.get("/health")
def health_check():
    return {"status": "ok"}


# --- HELPER FUNCTION: 3-COLOR VERDICT CLASSIFICATION ---
def get_ai_verdict(score: float) -> str:
    """
    Classifies the raw floating-point score into 3 distinct UI buckets:
    Score < 0.15              -> HUMAN (Green)
    0.15 <= Score <= 0.75     -> PARTIAL_AI (Yellow - 'May Be AI')
    Score > 0.75              -> AI (Red)

    Threshold lowered from 0.35 -> 0.15: hybrid / partially-AI images tend to
    produce a muted-but-nonzero score rather than a near-zero one, and were
    landing as HUMAN under the old cutoff.
    """
    if score < 0.15:
        return "HUMAN"
    elif score <= 0.75:
        return "PARTIAL_AI"
    else:
        return "AI"


# --- HELPER FUNCTION: GEMINI TRANSMISSION SANITIZER ---
def query_gemini_model(system_prompt: str, user_content: str) -> str:
    """
    Handles standard HTTP payload packaging, token handling, and cleans accidental
    markdown block formatting wrappers out of Gemini text responses.
    """
    if not GEMINI_API_KEY:
        print("[Security Warning] GEMINI_API_KEY is missing globally.")
        return "{}"

    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={GEMINI_API_KEY}"
    headers = {"Content-Type": "application/json"}
    
    payload = {
        "contents": [{
            "parts": [
                {"text": system_prompt},
                {"text": user_content}
            ]
        }]
    }
    
    try:
        response = requests.post(url, headers=headers, json=payload, timeout=UPSTREAM_TIMEOUT_SECONDS)
        response.raise_for_status()
        response_json = response.json()
        
        # Validating structural response payload blocks safely
        if 'candidates' not in response_json or not response_json['candidates']:
            print("[Gemini Layout Warning] Response payload structural layout mismatch.")
            return "{}"
            
        raw_text = response_json['candidates'][0]['content']['parts'][0]['text'].strip()
        # Regex pass to remove markdown indicators like ```json ... ``` cleanly
        return re.sub(r'^```json\s*|```$', '', raw_text, flags=re.IGNORECASE).strip()
    except Exception as e:
        print(f"[Gemini Transport Error] Transaction failed: {str(e)}")
        return "{}"


# --- 1. IMAGE VERIFICATION (Sightengine Dual-Model Shield) ---
@app.post("/verify")
async def verify_image(file: UploadFile = File(...), _auth: None = Depends(verify_app_key)):
    print("[Endpoint Triggered] /verify - Processing Image Payload")
    try:
        image_bytes = await file.read()
        if not image_bytes:
            return {"status": "failure", "error": "Empty file received", "ai_generated": 0.0, "verdict": "UNKNOWN"}

        # Decode-only sanity check: catches corrupt/non-image uploads early
        # with a clean error, WITHOUT re-encoding the file. We intentionally
        # do NOT resize or re-save as JPEG here anymore -- doing so on top of
        # the client's own JPEG export was a second lossy compression pass
        # that smoothed away exactly the subtle pixel-level artifacts
        # genai/deepfake detection relies on for partially-edited images.
        # The bytes the user actually captured now go to Sightengine as-is.
        try:
            sanity_img = Image.open(io.BytesIO(image_bytes))
            sanity_img.load()
        except Exception:
            return {"status": "failure", "error": "Invalid or corrupted image file", "ai_generated": 0.0, "verdict": "UNKNOWN"}

        # Fixed accuracy: We query BOTH 'genai' and 'deepfake' detection profiles simultaneously
        params = {
            'models': 'genai,deepfake', 
            'api_user': SIGHTENGINE_USER,
            'api_secret': SIGHTENGINE_SECRET
        }

        try:
            response = requests.post(
                'https://api.sightengine.com/1.0/check.json',
                files={'media': image_bytes},
                data=params,
                timeout=UPSTREAM_TIMEOUT_SECONDS
            )
        except requests.exceptions.Timeout:
            print("[Sightengine Timeout] Request exceeded the upstream timeout window.")
            return {
                "status": "failure",
                "error": "Image AI service timed out. Please try again.",
                "ai_generated": 0.0,
                "verdict": "UNKNOWN"
            }
        result = response.json()
        
        print(f"Sightengine RAW Dual-Response: {result}")

        # FIXED SECURITY HOLE: If API limit is reached, fail closed. Do not return "HUMAN"
        if result.get("status") == "failure":
            error_message = result.get("error", {}).get("message", "Sightengine API processing failure")
            print(f"[Sightengine Fail-Closed Action] Error: {error_message}")
            return {
                "status": "failure", 
                "error": f"Image API Error: {error_message}", 
                "ai_generated": 0.0, 
                "verdict": "UNKNOWN"
            }

        # Extract standard GenAI score (Global AI generation)
        genai_score = result.get("type", {}).get("ai_generated", 0.0)
        
        # Extract local face-swap / deepfake score if available
        deepfake_score = 0.0
        if "deepfake" in result:
            faces = result.get("deepfake", {}).get("faces", [])
            if faces:
                # Isolate the highest deepfake score discovered among present faces
                deepfake_score = max([face.get("score", 0.0) for face in faces])

        # Forensic Choice: Take the maximum score value between global AI and local deepfake manipulations
        final_score = max(genai_score, deepfake_score)
        print(f"[Forensic Evaluation] GenAI: {genai_score}, Deepfake: {deepfake_score} -> Chosen Final: {final_score}")
        
        return {
            "status": "success", 
            "ai_generated": final_score,
            "verdict": get_ai_verdict(final_score)
        }

    except Exception as e:
        print(f"Error in /verify: {str(e)}")
        # FIXED EXCEPTION HOLE: Force UNKNOWN state on structural server crashes
        return {
            "status": "failure", 
            "error": f"Backend Error: {str(e)}", 
            "ai_generated": 0.0, 
            "verdict": "UNKNOWN"
        }


# --- 2. TEXT VERIFICATION (Gemini Static Language Profiling) ---
@app.post("/verify-text")
async def verify_text(payload: dict = Body(...), _auth: None = Depends(verify_app_key)):
    try:
        text_content = payload.get("text_content", "")
        print(f"[Endpoint Triggered] /verify-text - Scanning via Gemini AI: {text_content}")
        
        if not text_content:
            return {"status": "failure", "error": "No text content", "ai_generated": 0.0, "verdict": "HUMAN"}

        system_prompt = (
            "You are an expert linguistic analysis AI for the OmniVerify platform. Analyze the perplexity, sentence structure, and style of the text provided.\n"
            "Determine the mathematical probability score that this text block was generated by an AI Large Language Model (like ChatGPT, Claude, or Gemini).\n"
            "Respond ONLY in a strict valid minified JSON object containing an 'ai_score' float between 0.0 (100% human) and 1.0 (100% AI machine generated):\n"
            "{\"ai_score\": 0.00}\n"
            "Do not write markdown formatting code blocks, backticks, conversation or text commentary."
        )

        clean_json_str = query_gemini_model(system_prompt, f"Text to analyze: {text_content}")
        
        if clean_json_str == "{}":
            raise ValueError("Empty transmission signature received from API cluster")

        parsed_result = json.loads(clean_json_str)
        ai_score = float(parsed_result.get("ai_score", 0.0))

        return {
            "status": "success", 
            "ai_generated": ai_score,
            "verdict": get_ai_verdict(ai_score)
        }
    except Exception as e:
        print(f"Error in Gemini Text Scan: {str(e)}")
        return {"status": "failure", "error": "Server network timeout. Try again.", "ai_generated": 0.0, "verdict": "UNKNOWN"}


# --- 3. LINK/URL VERIFICATION (Gemini Deep Heuristic Shield) ---
@app.post("/verify-link")
async def verify_link(payload: dict = Body(...), _auth: None = Depends(verify_app_key)):
    target_url = payload.get("url_content")
    print(f"[Endpoint Triggered] /verify-link - Scanning via Gemini Shield: {target_url}")
    
    if not target_url:
        return {"status": "failure", "error": "No URL provided"}

    system_prompt = (
        "You are an elite cyber threat intelligence analysis engine for the OmniVerify mobile safety app. "
        "Analyze the provided URL string very carefully. Check for indicators of phishing scams, credential harvesting clones, "
        "typosquatting (e.g., faceb00k instead of facebook), brand impersonation, deceptive query structures, or suspicious subdomains.\n"
        "Determine if this link represents an unsafe or malicious threat vector.\n"
        "Respond ONLY in a strict valid minified JSON object matching this exact structure without markdown backticks:\n"
        "{\"is_malicious\": true or false, \"verdict\": \"DANGEROUS\" or \"CLEAN\"}\n"
        "Do not write any markdown code blocks, conversations, thoughts or text commentary."
    )

    try:
        clean_json_str = query_gemini_model(system_prompt, f"URL to analyze: {target_url}")
        
        if clean_json_str == "{}":
            raise ValueError("Upstream API infrastructure timed out during execution sequence")

        parsed_result = json.loads(clean_json_str)
        is_malicious = parsed_result.get("is_malicious", False)
        verdict = parsed_result.get("verdict", "CLEAN")

        return {
            "status": "success",
            "is_malicious": is_malicious,
            "threat_counts": {
                "malicious": 1 if is_malicious else 0, 
                "phishing": 1 if is_malicious else 0, 
                "suspicious": 0
            },
            "verdict": verdict
        }
        
    except Exception as e:
        print(f"Error in Gemini Link Scan: {str(e)}")
        return {
            "status": "failure",
            "error": "Security validation timed out. Please retry scanning.",
            "is_malicious": True,
            "verdict": "UNKNOWN"
        }


# --- 4. INTELLIGENT QR CODE ROUTING ELEMENT ---
@app.post("/verify-qr")
async def verify_qr(payload: dict = Body(...), _auth: None = Depends(verify_app_key)):
    qr_content = payload.get("qr_content", "").strip()
    print(f"[Endpoint Triggered] /verify-qr - Processing Data: {qr_content}")
    
    if not qr_content:
        return {"status": "failure", "error": "Empty QR payload received"}

    # Step A: Regular Expression check for explicit web links
    is_url = re.match(r'^https?://', qr_content, re.IGNORECASE)
    
    # Step B: Fast-acting signature firewall check for standard terminal execution payloads
    is_script_signature = (
        "bin/bash" in qr_content or 
        "bin/sh" in qr_content or 
        qr_content.startswith("$ ") or 
        "sudo " in qr_content or
        (";" in qr_content and ("rm " in qr_content or "curl " in qr_content))
    )

    if is_url:
        print(f"[QR Router] Forwarding Link to Heuristic Gemini Shield Pipeline: {qr_content}")
        return await verify_link(payload={"url_content": qr_content})

    elif is_script_signature:
        print(f"[QR Router] Fast-Firewall Match: Identified explicit Shell Command layout patterns.")
        return {
            "status": "success",
            "is_malicious": True,
            "threat_counts": {"malicious": 1, "phishing": 0, "suspicious": 1},
            "verdict": "DANGEROUS_SCRIPT"
        }

    # Step C: Context analysis with Gemini SAST prompt if it skips the baseline signature firewall
    print(f"[QR Router] Analyzing text data blocks with Gemini SAST Intelligence Engine...")
    
    system_prompt = (
        "You are an automated static application security testing (SAST) analyzer for the OmniVerify mobile security app. "
        "Analyze the text provided by the user. Determine if it contains code representing malicious intent, unauthorized "
        "remote code execution, terminal exploit injection, reverse shells, or obfuscated cyber attack scripts (Bash, PowerShell, Python, SQL Injection, etc.). "
        "Respond ONLY in a strict, valid minified JSON object matching this structural pattern without markdown wrappers:\n"
        "{\"is_malicious\": true or false, \"verdict\": \"DANGEROUS_SCRIPT\" or \"CLEAN\"}\n"
        "Do not write any conversation, thoughts, explanation or markdown code block ticks."
    )
    
    try:
        clean_json_str = query_gemini_model(system_prompt, f"User text to scan: {qr_content}")
        
        if clean_json_str != "{}":
            ai_script_analysis = json.loads(clean_json_str)
            if ai_script_analysis.get("is_malicious") == True:
                print(f"[QR Router] ALERT: Gemini identified an obfuscated or hidden command execution threat payload!")
                return {
                    "status": "success",
                    "is_malicious": True,
                    "threat_counts": {"malicious": 1, "phishing": 0, "suspicious": 1},
                    "verdict": "DANGEROUS_SCRIPT"
                }
        else:
            raise ValueError("Internal SAST script transit route timed out")
            
    except Exception as e:
        print(f"[QR Router Error] Script verification sequence fallback: {str(e)}")
        return {
            "status": "failure",
            "error": "SAST analysis connection timeout.",
            "is_malicious": True,
            "verdict": "UNKNOWN"
        }

    # Step D: If clean of script signatures, evaluate plain text using the updated Gemini linguistic pipeline
    print(f"[QR Router] Script scan clear. Forwarding to Gemini Language Processing Pipeline...")
    return await verify_text(payload={"text_content": qr_content})