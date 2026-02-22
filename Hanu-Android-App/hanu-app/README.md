# 🅷 Hanu - PDF Converter App

Convert any PDF to Excel, Word, or PowerPoint — completely offline, fast, and free.

---

## ✨ Features

- 📊 **PDF → Excel (.xlsx)** — Extracts text as structured rows & columns with styling
- 📝 **PDF → Word (.docx)** — Full text extraction with headings and page breaks
- 📈 **PDF → PowerPoint (.pptx)** — Each PDF page becomes a high-quality slide
- 🔒 **100% Offline** — No internet required, no data uploaded
- ⚡ **Fast conversion** with real-time progress tracking
- 📤 **Share or open** output files directly
- 🎨 Beautiful dark UI with smooth animations

---

## 📁 Project Structure

```
hanu-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/hanu/pdfconverter/
│   │   │   ├── SplashActivity.java      ← Animated splash screen
│   │   │   ├── MainActivity.java        ← File picker + format selector
│   │   │   ├── ConvertActivity.java     ← Conversion progress screen
│   │   │   └── PdfConverter.java        ← Core conversion engine
│   │   ├── res/
│   │   │   ├── layout/                  ← UI layouts
│   │   │   ├── drawable/                ← Icons, shapes, drawables
│   │   │   └── values/                  ← Colors, strings, themes
│   │   └── AndroidManifest.xml
│   └── build.gradle                     ← App dependencies
└── build.gradle                         ← Root build config
```

---

## 🛠️ HOW TO BUILD THE APK

### Prerequisites
- **Android Studio** (free): https://developer.android.com/studio
- **JDK 11+** (comes with Android Studio)
- ~4 GB free disk space

### Step 1 — Open the project
1. Open **Android Studio**
2. Click **"Open"** → navigate to the `hanu-app` folder → click OK
3. Wait for Gradle to sync (first time takes ~5 minutes — downloads dependencies)

### Step 2 — Build APK
**Option A — Debug APK (for testing):**
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```
APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

**Option B — Release APK (for sharing/Play Store):**
```
Build → Generate Signed Bundle / APK → APK → Create new keystore → Follow wizard
```
APK will be at: `app/build/outputs/apk/release/app-release.apk`

**Option C — Command line:**
```bash
cd hanu-app
./gradlew assembleDebug
```

---

## 📱 HOW TO INSTALL ON YOUR PHONE

### Method 1 — Direct Install (USB)
1. Enable **Developer Options** on your phone:
   - Settings → About Phone → tap **Build Number** 7 times
2. Enable **USB Debugging** in Developer Options
3. Connect phone via USB → Android Studio shows your device
4. Click **▶ Run** in Android Studio

### Method 2 — APK File Transfer
1. Build the APK (see above)
2. Transfer `app-debug.apk` to your phone (WhatsApp, email, USB, etc.)
3. On phone: Settings → Security → **Enable "Install Unknown Apps"**
4. Open the APK file → Install

---

## 🚀 HOW TO PUBLISH ON GOOGLE PLAY STORE (Free)

### Step 1 — Create a Google Play Developer Account
1. Go to: https://play.google.com/console
2. Sign in with Google account
3. Pay **one-time $25 registration fee**
4. Fill in developer profile

### Step 2 — Create a Signed Release APK
1. In Android Studio: **Build → Generate Signed Bundle / APK**
2. Choose **APK** → Next
3. Click **Create new keystore**:
   - Choose a safe location (NEVER lose this file!)
   - Set passwords and key alias
4. Fill in certificate details → Finish
5. Select **release** build variant → Finish
6. APK is at: `app/build/outputs/apk/release/app-release.apk`

### Step 3 — Create the App on Play Console
1. Go to Google Play Console → **Create app**
2. Fill in:
   - App name: **Hanu**
   - Default language: English
   - App type: App
   - Free / Paid: Free
3. Complete the **Store listing**:
   - Short description (80 chars max)
   - Full description
   - Screenshots (at least 2)
   - Feature graphic (1024×500 px)
   - App icon (512×512 px)

### Step 4 — Upload APK
1. Go to **Production → Create new release**
2. Upload your `app-release.apk`
3. Write release notes
4. Review and **Rollout to Production**

### Step 5 — Complete Required Sections
- **App content** → privacy policy, ads, target audience
- **Privacy policy**: Use https://privacypolicytemplate.net (free)
- **Pricing**: Set to Free

### ⏱️ Review time: 2–7 days for first app

---

## 🆓 FREE ALTERNATIVES TO PLAY STORE

If you don't want to pay the $25 Play Store fee:

| Platform | Cost | Link |
|---|---|---|
| **APKPure** | Free | apkpure.com/developer |
| **F-Droid** | Free (open source only) | f-droid.org |
| **Amazon Appstore** | Free | developer.amazon.com |
| **Samsung Galaxy Store** | Free | seller.samsungapps.com |
| **Direct APK share** | Free | Send APK via WhatsApp/email |

---

## 🔧 Customization

### Change app name
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">Your App Name</string>
```

### Change colors
Edit `app/src/main/res/values/colors.xml`:
```xml
<color name="accent_green">#YOUR_COLOR</color>
```

### Add real AI-based OCR
For scanned PDFs (image-based), integrate:
- **Google ML Kit** (free, on-device): `com.google.mlkit:text-recognition:16.0.0`
- **Tesseract4Android** (free, open source)

---

## 📋 Dependencies Used

| Library | Purpose |
|---|---|
| Apache POI 5.2.3 | Excel, Word, PowerPoint generation |
| PDFBox Android 2.0.27 | PDF text extraction |
| Android PdfRenderer | PDF page rendering (built-in) |
| Material Components 1.11 | UI components |
| Lottie 6.3 | Animations |

---

## 📄 License
MIT License — free to use, modify, and distribute.

---

**Built with ❤️ — Hanu PDF Converter**
