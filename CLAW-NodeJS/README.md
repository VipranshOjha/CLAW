# 🌐 CLAW-NodeJS (Legacy Implementation)

**Control Laptop Anytime Wirelessly via Web-Socket Architecture.**

This folder contains the original dual-component version of C.L.A.W. It uses a **Node.js server** as a bridge between your phone's web browser and your laptop's OS. This version is perfect for users who want a "No-Install" experience on their smartphone.

---

## 🛠️ Tech Stack
- **Backend:** Node.js, Express
- **Communication:** Socket.io (WebSockets)
- **Input Simulation:** RobotJS / ViGEmBus (for virtual Xbox 360 emulation)
- **Frontend:** HTML5, CSS3, Vanilla JavaScript

## ⚡ How it Works
1. The **Node.js Server** runs on your laptop.
2. Your **Smartphone** connects to the server's IP address via a web browser.
3. Touch inputs on the phone are sent as JSON packets via **WebSockets**.
4. The server receives the packets and uses **RobotJS** to simulate keyboard strokes or **ViGEm** to simulate gamepad axes.

---

## 🚀 Quick Setup Guide

### 1. Prerequisites
- **Node.js** (v14 or higher) installed on your PC.
- (Optional) **ViGEmBus** drivers installed if you want to emulate a physical Xbox controller.

### 2. Installation
```
# Navigate to the NodeJS folder
cd CLAW-NodeJS

# Install dependencies
npm install
```

### 3. Execution
```
# Start the server
npm start
```

### 4. Connection
- The terminal will display a Local IP Address (e.g., 192.168.1.15:3000).

- Open this address on your phone’s browser.

- Position your hands in a "Claw Grip" and start gaming!

### 🕹️ Control Layout (Web Mapping)
This version replicates a Keyboard + Mouse setup by default, ideal for FPS and strategy games:
```
┌──────────────┬──────────────┬──────────────┐
│    MOVEMENT  │     CLAW     │    ACTIONS   │
│    (WASD)    │   TRACKPAD   │    BUTTONS   │
│              │              │              │
│      W       │ ┌──────────┐ │     JUMP     │
│   A  ✚  D   │ │ PRECISION│ │    (SPACE)   │  
│      S       │ │  CURSOR  │ │              │
│              │ │ CONTROL  │ │    SPRINT    │
│    WASD      │ └──────────┘ │    (SHIFT)   │
│   Movement   │   Tap/Drag   │              │
└──────────────┴──────────────┴──────────────┘
```

### ⚠️ Limitations vs. CLAW-Android
Latency: Dependent on Wi-Fi network stability.

Drivers: Requires a local server and simulation drivers (RobotJS) to be running on the host PC.

Backgrounding: Connection may drop if the phone screen turns off or the browser is minimized.
