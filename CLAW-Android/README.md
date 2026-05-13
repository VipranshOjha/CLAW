# 📱 CLAW-Android (Native HID Implementation)

**Zero-Driver Bluetooth Gamepad Hardware Emulator.**

This folder contains the professional-grade evolution of C.L.A.W. Unlike the legacy version, this implementation uses the **Android Bluetooth HID (Human Interface Device) Profile** to make your smartphone appear to your laptop as a physical hardware controller (like an Xbox One or PS5 controller).

---

## 🛠️ Tech Stack
- **Native Backend:** Kotlin, Android Bluetooth Stack
- **Protocol:** Bluetooth L2CAP (HID Profile)
- **Frontend:** HTML5, CSS3, Vanilla JavaScript (Internal Assets)
- **Bridge:** JavascriptInterface (WebView-to-Kotlin Native Bridge)

## ⚡ Key Advantages (The "Lite" Evolution)
1. **Zero PC Drivers:** No need to install Node.js, RobotJS, or ViGEmBus on your computer. If your PC has Bluetooth, it works.
2. **Hardware-Level Latency:** By bypassing the Wi-Fi stack and moving to Bluetooth L2CAP, input lag is significantly reduced.
3. **Universal Compatibility:** Works with Steam Big Picture, Xbox Game Pass for PC, and any game that supports standard HID controllers.
4. **Standalone:** The phone handles all the logic. Your PC simply sees a "Gamepad."

---

## 🚀 Setup & Installation

### 1. Build the App
- Open this folder (`CLAW-Android`) in **Android Studio**.
- Ensure **USB Debugging** and **USB Debugging (Security Settings)** are enabled on your device (especially for Xiaomi/POCO users).
- Click **Run** to install the APK on your smartphone.

### 2. Connect to Laptop (Windows 11)
- **On Phone:** Open the CLAW app and stay on the screen.
- **On Laptop:** Go to `Settings > Bluetooth & devices`.
- **CRITICAL:** Change "Bluetooth devices discovery" from **Default** to **Advanced**.
- Click **Add device** and select **CLAW Lite Gamepad**.

### 3. Verify Connection
- Press `Win + R` and type `joy.cpl`.
- You should see **HID-compliant game controller** with a status of **OK**.
- Test your buttons and analog sticks at [Gamepad-Tester.com](https://gamepad-tester.com/).

---

## 🕹️ Xbox Controller Mapping
The UI is mathematically mapped to standard Xbox 360/One report structures:

- **Byte 0:** ABXY, LB/RB, Back/Start
- **Byte 1:** LS/RS Clicks, LT/RT Digital
- **Byte 2:** 8-way D-Pad (Hat Switch)
- **Bytes 3-6:** Left & Right Analog Sticks (-127 to 127 range)

---

## 📂 Internal Project Structure
```text
app/src/main/
├── java/com/example/clawlite/
│   └── MainActivity.kt      <-- Bluetooth HID Logic & SDP Records
└── assets/
    ├── index.html           <-- Gamepad UI
    ├── style.css            <-- Neon Gamer Aesthetics
    └── client.js            <-- Touch-to-HID Bitwise Engine
