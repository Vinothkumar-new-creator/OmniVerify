from fastapi import FastAPI, UploadFile, File, Body
from fastapi.middleware.cors import CORSMiddleware
import requests
from bs4 import BeautifulSoup
from PIL import Image, ImageOps
import io
import time
import base64
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
SAPLING_API_KEY = os.environ.get("SAPLING_API_KEY")
VIRUSTOTAL_API_KEY = os.environ.get("VIRUSTOTAL_API_KEY")
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY") # New key for AI Script Analysis

# --- HELPER FUNCTION: 3-COLOR VERDICT CLASSIFICATION ---
def get_ai_verdict(score: float) -> str:
    """
    Classifies the raw floating-point score into 3 distinct UI buckets:
    Score < 0.35              -> HUMAN (Green)
    0.00 <= Score <= 0.75     -> PARTIAL_AI (Yellow)
    Score > 0.75              -> AI (Red)
    """
    if score < 0.35:
        return "HUMAN"
    elif score <= 0.75:
        return "PARTIAL_AI"
    else:
        return "AI"

# --- HELPER FUNCTION: GEMINI AI SCRIPT ANALYZER ---
def analyze_script_with_gemini(code_content: str) -> dict:
    """
    Uses Gemini API to analyze if a text string contains a malicious script or command injection.
    """
    if not GEMINI_API_KEY:
        print("[Security Warning] GEMINI_API_KEY is missing. Falling back to default baseline handling.")
        return {"is_malicious": False, "verdict": "CLEAN"}

    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={GEMINI_API_KEY}"
    headers = {"Content-Type": "application/json"}
    
    system_prompt = (
        "You are an automated static application security testing (SAST) analyzer for the OmniVerify mobile security app. "
        "Analyze the text provided by the user. Determine if it contains code representing malicious intent, unauthorized "
        "remote code execution, terminal exploit injection, reverse shells, or obfuscated cyber attack scripts (Bash, PowerShell, Python, SQL Injection, etc.). "
        "Respond ONLY in a strict, valid minified JSON object matching this structural pattern without markdown wrappers:\n"
        "{\"is_malicious\": true or false, \"verdict\": \"DANGEROUS_SCRIPT\" or \"CLEAN\"}\n"
        "Do not write any conversation, thoughts, explanation or markdown code block ticks."
    )

    payload = {
        "contents": [{
            "parts": [
                {"text": system_prompt},
                {"text": f"User text to scan: {code_content}"}
            ]
        }]
    }

    try:
        response = requests.post(url, headers=headers, json=payload, timeout=10)
        response_json = response.json()
        
        # Extract the text output from Gemini payload response structure
        raw_text = response_json['candidates'][0]['content']['parts'][0]['text'].strip()
        
        # Clean any accidental markdown block formatting backticks if added by the LLM
        clean_json = re.sub(r'^```json\s*|```$', '', raw_text, flags=re.IGNORECASE).strip()
        
        return json.loads(clean_json)
    except Exception as e:
        print(f"Gemini API Analysis failed: {str(e)}")
        return {"is_malicious": False, "verdict": "CLEAN"}


# --- 1. IMAGE VERIFICATION (Sightengine) ---
@app.post("/verify")
async def verify_image(file: UploadFile = File(...)):
    try:
        image_bytes = await file.read()
        if not image_bytes:
            return {"status": "failure", "error": "Empty file received", "ai_generated": 0.0, "verdict": "HUMAN"}

        img = Image.open(io.BytesIO(image_bytes))
        img = ImageOps.exif_transpose(img)
        
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")

        buffer = io.BytesIO()
        img.save(buffer, format="JPEG", quality=85) 
        optimized_bytes = buffer.getvalue()
        
        params = {
            'models': 'genai', 
            'api_user': SIGHTENGINE_USER,
            'api_secret': SIGHTENGINE_SECRET
        }
        
        response = requests.post('https://api.sightengine.com/1.0/check.json', 
                                 files={'media': optimized_bytes}, data=params)
        result = response.json()
        
        print(f"Sightengine RAW Response: {result}")

        if result.get("status") == "failure":
            return {"status": "failure", "error": result.get("error", {}).get("message"), "ai_generated": 0.0, "verdict": "HUMAN"}

        ai_val = result.get("type", {}).get("ai_generated", 0.0)
        
        return {
            "status": "success", 
            "ai_generated": ai_val,
            "verdict": get_ai_verdict(ai_val)
        }

    except Exception as e:
        print(f"Error in /verify: {str(e)}")
        return {"status": "failure", "error": str(e), "ai_generated": 0.0, "verdict": "HUMAN"}


# --- 2. TEXT VERIFICATION (Sapling AI) ---
@app.post("/verify-text")
async def verify_text(payload: dict = Body(...)):
    try:
        text_content = payload.get("text_content", "")
        if not text_content:
            return {"status": "failure", "error": "No text content", "ai_generated": 0.0, "verdict": "HUMAN"}

        url = "https://api.sapling.ai/api/v1/aidetect"
        response = requests.post(url, json={"key": SAPLING_API_KEY, "text": text_content})
        result = response.json()
        
        score = result.get("score", 0.0)
        
        return {
            "status": "success", 
            "ai_generated": score,
            "verdict": get_ai_verdict(score)
        }
    except Exception as e:
        return {"status": "failure", "error": str(e), "ai_generated": 0.0, "verdict": "HUMAN"}


# --- 3. LINK/URL VERIFICATION (VirusTotal) ---
@app.post("/verify-link")
async def verify_link(payload: dict = Body(...)):
    target_url = payload.get("url_content")
    if not target_url:
        return {"status": "failure", "error": "No URL provided"}

    try:
        url_id = base64.urlsafe_b64encode(target_url.encode()).decode().strip("=")
        vt_url = f"https://www.virustotal.com/api/v3/urls/{url_id}"
        headers = {
            "accept": "application/json",
            "x-apikey": VIRUSTOTAL_API_KEY
        }

        response = requests.get(vt_url, headers=headers)
        
        if response.status_code == 404:
            submit_url = "https://www.virustotal.com/api/v3/urls"
            requests.post(submit_url, data={"url": target_url}, headers=headers)
            return {"status": "processing", "message": "Link is being scanned for the first time. Try again in 30 seconds."}

        result = response.json()
        stats = result['data']['attributes']['last_analysis_stats']
        
        is_malicious = stats['malicious'] > 0 or stats['phishing'] > 0
        
        return {
            "status": "success",
            "is_malicious": is_malicious,
            "threat_counts": {
                "malicious": stats['malicious'],
                "phishing": stats['phishing'],
                "suspicious": stats['suspicious']
            },
            "verdict": "DANGEROUS" if is_malicious else "CLEAN"
        }
        
    except Exception as e:
        return {"status": "failure", "error": f"Security scan failed: {str(e)}"}


# --- 4. INTELLIGENT QR CODE ROUTING ELEMENT ---
@app.post("/verify-qr")
async def verify_qr(payload: dict = Body(...)):
    qr_content = payload.get("qr_content", "").strip()
    if not qr_content:
        return {"status": "failure", "error": "Empty QR payload received"}

    # Route Step 1: Check if the payload is a Web URL
    is_url = re.match(r'^https?://', qr_content, re.IGNORECASE)
    if is_url:
        print(f"[QR Router] Forwarding Link to VirusTotal: {qr_content}")
        return await verify_link(payload={"url_content": qr_content})

    # Route Step 2: Use Gemini AI to deeply analyze text for malicious script execution code blocks
    print(f"[QR Router] Analyzing payload text with Gemini AI Engine...")
    ai_script_analysis = analyze_script_with_gemini(qr_content)
    
    if ai_script_analysis.get("is_malicious") == True:
        print(f"[QR Router] ALERT: Gemini identified a malicious threat vector payload!")
        return {
            "status": "success",
            "is_malicious": True,
            "threat_counts": {"malicious": 1, "phishing": 0, "suspicious": 1},
            "verdict": "DANGEROUS_SCRIPT"  // Alerts your UI red status configuration
        }
    
    # Route Step 3: If clean of scripts, run normal Natural Language verification via Sapling
    print(f"[QR Router] Script scan clear. Running structural text analysis via Sapling AI...")
    return await verify_text(payload={"text_content": qr_content})
