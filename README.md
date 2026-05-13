# 🎮 C.L.A.W. - Control Laptop Anytime Wirelessly

**Master your games with the precision of a predator.**

C.L.A.W. is a dual-architecture project designed to transform your smartphone into a high-performance wireless gaming controller. Inspired by the "Claw Grip" technique used by elite esports athletes, this project bridges the gap between mobile convenience and PC gaming precision.

Whether you need a quick setup via a web browser or a professional-grade, zero-driver hardware emulator, C.L.A.W. has an implementation for you.

---

### 🎯 The Claw Grip Advantage
In competitive gaming, the "Claw Grip" allows for simultaneous movement, aiming, and action execution. C.L.A.W. brings this philosophy to your phone:

- **Lightning Reflexes**: Execute complex combos and simultaneous actions.
- **Ergonomic Precision**: Custom-mapped touch zones designed for your natural hand position.
- **Zero Latency**: Optimized data transmission paths for split-second reaction times.

---

## ⚡ Why C.L.A.W.?

| Feature | Standard Controller | **C.L.A.W.** |
| :--- | :--- | :--- |
| **Cost** | $50 - $200+ | **FREE** |
| **Portability** | Bulky extra device | **Just your phone** |
| **Customization** | Fixed hardware buttons | **Infinite UI possibilities** |
| **Versatility** | Hardware locked | **Web-based OR Native HID** |

---

## 🚀 Choose Your Implementation

This repository contains two distinct ways to turn your phone into a controller. Choose the one that fits your setup:

### 1. [CLAW-Android](./CLAW-Android) (The Pro Version) 📱
**Tech Stack:** Kotlin, Android Bluetooth HID Profile, HTML/CSS/JS Assets.
- **How it works:** Your phone identifies itself as a **Real Bluetooth Gamepad** (HID). 
- **Pros:** No drivers needed on PC, works with any game that supports Xbox/Generic controllers, ultra-low latency.
- **Best for:** Competitive gaming, Steam, and console-style experiences.

### 2. [CLAW-NodeJS](./CLAW-NodeJS) (The Legacy Version) 🌐
**Tech Stack:** Node.js, Socket.io, RobotJS/ViGEmBus.
- **How it works:** A local server on your laptop receives inputs from your phone's browser via Wi-Fi.
- **Pros:** Instant setup via QR code, no app installation required on the phone.
- **Best for:** Casual gaming, quick sessions, and devices where you can't install apps.

---

## 🤝 Contributing
C.L.A.W. is an open-source project. If you have ideas for new layouts, better latency optimization, or gesture controls, feel free to fork the repo and submit a PR!

## 📄 License
Licensed under the **MIT License**. Use it, mod it, and play freely.

---

### **Ready to Unleash the CLAW?**
Navigate into either [**CLAW-Android**](./CLAW-Android) or [**CLAW-NodeJS**](./CLAW-NodeJS) to get started with the specific setup guides!
