# OmniVerify

OmniVerify is a comprehensive verification and threat analysis platform. It features an **Android Mobile Application** and a **FastAPI Python Backend** to perform image verification, text authenticity checking, and malicious link detection.

---

## 📁 Project Structure

This repository is organized into the following main directories:

- **[`OmniVerifyBackend/`](./OmniVerifyBackend)**: The FastAPI server that handles image analysis, text scanning, and security verification.
- **[`omniverify.1/`](./omniverify.1)**: The Android app built using Kotlin and Jetpack Compose.
- **`finalrop_all.pdf`**: Design and security specifications documentation.

---

## 🚀 Getting Started

### 1. Backend Setup (`OmniVerifyBackend`)

The backend is built with Python 3.x and FastAPI. It communicates with external APIs to verify images, text, and URLs.

#### **Prerequisites**
You need API credentials from the following providers:
- [Sightengine](https://sightengine.com/) (Image verification)
- [Sapling AI](https://sapling.ai/) (Text authenticity checking)
- [VirusTotal](https://www.virustotal.com/) (Link security scanning)

#### **Installation & Running**
1. Navigate to the backend directory:
   ```bash
   cd OmniVerifyBackend
   ```
2. Install the required dependencies:
   ```bash
   pip install fastapi uvicorn requests beautifulsoup4 pillow
   ```
3. Run the FastAPI development server:
   ```bash
   python -m uvicorn main:app --host 0.0.0.0 --port 8000
   ```
   The API will be available at `http://localhost:8000`. You can access interactive documentation at `http://localhost:8000/docs`.

---

### 2. Android App Setup (`omniverify.1`)

The Android application is built using modern Kotlin gradle syntax.

#### **Prerequisites**
- Android Studio (Jellyfish or newer recommended)
- Android SDK (Level 34+)
- JDK 17+

#### **Running the App**
1. Open Android Studio.
2. Select **Open An Existing Project** and choose the `omniverify.1` directory.
3. Allow Gradle sync to complete.
4. Run the app on an Emulator or connected Physical Device.

---

## 🔒 Security Best Practices

> [!WARNING]
> Before pushing this codebase to a public GitHub repository, ensure you have **removed all hardcoded API keys** from `main.py`.
> Instead, load them from environment variables or a `.env` file.

---

## 🛠️ API Endpoints

The backend provides the following POST endpoints:
- `POST /verify`: Scans uploaded images for AI-generated content (Sightengine).
- `POST /verify-text`: Analyzes text for AI generation probability (Sapling AI).
- `POST /verify-link`: Security check for URLs (VirusTotal).
