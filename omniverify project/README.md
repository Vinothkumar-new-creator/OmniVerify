# OmniVerify 🛡️

**OmniVerify** is a professional-grade Android security application designed to protect users from synthetic media, malicious links, and fraudulent QR codes. By leveraging advanced AI detection and real-time monitoring, OmniVerify ensures your digital interactions remain authentic and secure.

---

## ✨ Key Features

### 🔍 AI Detection & Analysis
Identify synthetic images, deepfakes, and AI-generated content with high precision. Our backend uses state-of-the-art models to provide a probability score and a clear verdict (Human vs. AI).

### 🚀 Floating Assistant (Real-time Protection)
A persistent, non-intrusive floating bubble that allows you to analyze any content on your screen instantly.
- **Screen Crop & Scan**: Select any part of your screen for immediate analysis.
- **On-the-fly QR Decoding**: Verify QR codes within other apps without switching context.
- **Quick Text/Link Check**: Paste suspicious snippets or URLs for a safety audit.

### 📱 Multimedia Scanner
Deep-dive analysis for local media:
- **Image Scanner**: Upload photos from your gallery for forensic AI inspection.
- **QR Scanner**: Securely decode and verify the safety of QR destinations.
- **Text & Link Forensic**: Paste content to check for phishing patterns or synthetic origins.

### 🔒 Privacy-First Architecture
We prioritize your data security. Analysis is performed with minimal data retention, and history is stored locally on your device using an encrypted Room database.

---

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: Modern Android XML with Material Design 3
- **Local Database**: Room Persistence Library
- **Networking**: Retrofit 2 & OkHttp 3
- **ML Engine**: Google ML Kit (Barcode Scanning)
- **Background Processing**: Android Services & Media Projection API
- **Architecture**: MVVM (Model-View-ViewModel)

---

## 🚀 How to Use

1. **Activate Protection**: Tap the large shield button on the Home Screen to start the Floating Assistant.
2. **Access History**: Click the menu icon (top-left) to view your previous scans and verdicts.
3. **Use the Assistant**:
    - Tap the floating icon to expand the menu.
    - Use the **Crop Tool** to select an area of your screen.
    - View results in the **Mini Popup** without leaving your current app.
4. **Manual Scans**: Navigate to the "Multimedia" tab to upload files or paste text for analysis.

---

## 📥 Installation

### Download APK
You can download the latest stable release from the [Releases](https://github.com/YOUR_USERNAME/omniverify/releases) page.

### Build from Source
1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/omniverify.git
   ```
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Sync Gradle and ensure all dependencies are downloaded.
4. Build and Run the `app` module on a physical device or emulator.

---

## 🛡️ Privacy & Permissions
To provide real-time protection, OmniVerify requires:
- **Overlay Permission**: To show the Floating Assistant over other apps.
- **Screen Recording (Media Projection)**: To analyze content you specifically select via the crop tool.
- **Camera**: For direct scanning of physical QR codes/documents.

---

## 🤝 Contributing
Contributions are welcome! If you have ideas for new features or find a bug, please open an issue or submit a pull request.

---

## 📄 License
Copyright © 2024 OmniVerify. Distributed under the [MIT License](LICENSE).
