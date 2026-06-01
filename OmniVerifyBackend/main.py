from fastapi import FastAPI, UploadFile, File, Body
import requests
from bs4 import BeautifulSoup
from PIL import Image, ImageOps
import io
import time
import base64

app = FastAPI()

# --- CONFIGURATION ---
SIGHTENGINE_USER="your_actual_user_id_here"
SIGHTENGINE_SECRET="your_actual_secret_here"
SAPLING_API_KEY="your_actual_sapling_key_here"
VIRUSTOTAL_API_KEY = "your_actual_key_here"

# --- 1. IMAGE VERIFICATION (Sightengine) ---
@app.post("/verify")
async def verify_image(file: UploadFile = File(...)):
    try:
        image_bytes = await file.read()
        if not image_bytes:
            return {"status": "failure", "error": "Empty file received", "ai_generated": 0.0}

        img = Image.open(io.BytesIO(image_bytes))
        
        # Fix Orientation to ensure consistency
        img = ImageOps.exif_transpose(img)
        
        # Convert to RGB if it's RGBA (PNG with transparency) to avoid Sightengine errors
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")

        buffer = io.BytesIO()
        img.save(buffer, format="JPEG", quality=85) # Slightly higher quality for better consistency
        optimized_bytes = buffer.getvalue()
        
        params = {
            'models': 'genai', 
            'api_user': SIGHTENGINE_USER,
            'api_secret': SIGHTENGINE_SECRET
        }
        
        response = requests.post('https://api.sightengine.com/1.0/check.json', 
                                 files={'media': optimized_bytes}, data=params)
        
        result = response.json()
        
        # DEBUG: Print this in your terminal to see the raw score
        print(f"Sightengine RAW Response: {result}")

        if result.get("status") == "failure":
            return {"status": "failure", "error": result.get("error", {}).get("message"), "ai_generated": 0.0}

        # Extract score correctly
        ai_val = result.get("type", {}).get("ai_generated", 0.0)
        
        return {"status": "success", "ai_generated": ai_val}

    except Exception as e:
        print(f"Error in /verify: {str(e)}")
        return {"status": "failure", "error": str(e), "ai_generated": 0.0}

# --- 2. TEXT VERIFICATION (Sapling AI) ---
@app.post("/verify-text")
async def verify_text(payload: dict = Body(...)):
    try:
        text_content = payload.get("text_content", "")
        if not text_content:
            return {"status": "failure", "error": "No text content", "ai_generated": 0.0}

        url = "https://api.sapling.ai/api/v1/aidetect"
        response = requests.post(url, json={"key": SAPLING_API_KEY, "text": text_content})
        result = response.json()
        
        # Sapling usually returns "score" between 0 and 1
        score = result.get("score", 0.0)
        
        return {"status": "success", "ai_generated": score}
    except Exception as e:
        return {"status": "failure", "error": str(e), "ai_generated": 0.0}

# --- 3. LINK/URL VERIFICATION ---
@app.post("/verify-link")
async def verify_link(payload: dict = Body(...)):
    target_url = payload.get("url_content")
    if not target_url:
        return {"status": "failure", "error": "No URL provided"}

    try:
        # VirusTotal v3 requires the URL to be Base64 encoded without padding
        url_id = base64.urlsafe_b64encode(target_url.encode()).decode().strip("=")
        
        vt_url = f"https://www.virustotal.com/api/v3/urls/{url_id}"
        headers = {
            "accept": "application/json",
            "x-apikey": VIRUSTOTAL_API_KEY
        }

        response = requests.get(vt_url, headers=headers)
        
        # If URL is brand new, VirusTotal might return 404. 
        # In that case, we must "submit" it for scanning first.
        if response.status_code == 404:
            submit_url = "https://www.virustotal.com/api/v3/urls"
            requests.post(submit_url, data={"url": target_url}, headers=headers)
            return {"status": "processing", "message": "Link is being scanned for the first time. Try again in 30 seconds."}

        result = response.json()
        stats = result['data']['attributes']['last_analysis_stats']
        
        # logic: if more than 0 engines flag it, it's potentially malicious
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
# Run: python -m uvicorn main:app --host 0.0.0.0 --port 8000
