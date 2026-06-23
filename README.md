# 🛡️ OmniVerify

**OmniVerify** is an Android app that helps you spot AI-generated images, deepfakes, phishing links, and malicious QR codes — without ever leaving the app you're in. A floating assistant overlays on top of WhatsApp, Instagram, or any other app, so you can crop, scan, and verify suspicious content the moment you see it.

---

## ✨ Key Features

### 🔍 AI Image & Text Detection
Upload a photo or paste a block of text to get a clear verdict — **Human**, **May Be AI**, or **AI Generated** — backed by a confidence score.

### 🚀 Floating Assistant (Real-Time Protection)
A lightweight floating bubble that stays on screen while you browse other apps:
- **Screen Crop & Scan** — select any part of your screen for instant analysis
- **On-the-fly QR Decoding** — verify QR codes without leaving your current app
- **Quick Text/Link Check** — paste a snippet or URL for a fast safety check

### 📱 Multimedia Scanner
A dedicated tab for deliberate, deep-dive scans:
- **Image Scanner** — analyze photos from your gallery or camera
- **QR Scanner** — decode a QR code and check where it actually leads
- **Text & Link Forensics** — check pasted text or URLs for phishing patterns or AI-generated language

### 🕘 Scan History
Every scan is saved locally on-device (Room database) so you can revisit past results.

---

## 🛠️ Tech Stack

**Android App**
- Kotlin, MVVM, Android Views + Material Design 3
- Room (local scan history)
- Retrofit 2 + OkHttp (networking)
- Google ML Kit (Barcode/QR scanning)
- Android Services + MediaProjection API (floating assistant & screen capture)

**Backend**
- Python + FastAPI
- [Sightengine](https://sightengine.com/) for image-based AI/deepfake detection
- [Google Gemini](https://ai.google.dev/) for text, link, and QR-payload analysis
- Deployed on Render

---

## 🚀 Getting Started

### Download
Grab the latest APK from the [Releases](https://github.com/YOUR_USERNAME/omniverify/releases) page.

### Build from Source
```bash
git clone https://github.com/YOUR_USERNAME/omniverify.git
```
1. Open the project in **Android Studio** (Ladybug or newer).
2. Let Gradle sync and download dependencies.
3. Run the `app` module on a device or emulator (Android 8.0 / API 26+).

### Backend Setup
The Android app talks to a small FastAPI backend (`OmniVerifyBackend/main.py`). To run your own instance:
```bash
cd OmniVerifyBackend
pip install -r requirements.txt
uvicorn main:app --reload
```
Set these environment variables before starting the server:

| Variable | Purpose |
|---|---|
| `SIGHTENGINE_USER` / `SIGHTENGINE_SECRET` | Image AI & deepfake detection |
| `GEMINI_API_KEY` | Text, link, and QR-payload analysis |

Update `BASE_URL` in `NetworkClient.kt` to point at your own backend if you're not using the hosted one.

---

## 📲 How to Use

1. **Activate Protection** — tap the shield button on the Home screen to start the floating assistant.
2. **Scan in any app** — tap the floating icon, then crop the area you want checked.
3. **View results instantly** — a mini popup shows the verdict without leaving your current app.
4. **Manual scans** — head to the **Multimedia** tab to upload an image or paste text/links directly.
5. **Review history** — tap the menu icon (top-left) on the Home screen to see past scans.

---

## 🔒 Permissions

| Permission | Why it's needed |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Show the floating assistant over other apps |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Keep the assistant running and capture the screen area you select |
| `CAMERA` | Take photos for direct image/QR scanning |
| `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` (≤ API 32) | Let you pick images from your gallery |

Scan history is stored locally in a Room database and is not currently encrypted at rest — see [Known Limitations](#-known-limitations--roadmap) below.

---

## ⚠️ Known Limitations & Roadmap

OmniVerify is under active development. A few things worth knowing before you rely on it:

- **No backend authentication yet** — the API currently accepts requests from anyone who has the endpoint URL, not just the app. Rate limiting / request signing is planned.
- **Deepfake detection is face-specific** — by design, the deepfake model only flags manipulated *faces*. Partially AI-edited (hybrid) images without a face swap rely on the general AI-detection model instead, which can be less sensitive to small or localized edits.
- **Local scan history is not encrypted** — it's stored in a plain Room database and is included in Android backups. Encryption is on the roadmap.
- **No automated tests yet** for the detection pipeline beyond manual spot-checks.

Found something else? Please open an issue.

---

## 🤝 Contributing

Issues and pull requests are welcome! If you find a bug or have an idea for a new feature, please open an issue first so we can discuss the approach.

---

## 📄 License

This project does not yet include a license file. Until one is added, all rights are reserved by the author — please reach out before reusing the code.
