# <img src="icon.png" width="250" height="250" valign="middle"> GutAngle Application

[![BioMedical Engineering](https://img.shields.io/badge/Domain-Biomedical%20Engineering-blue.svg)](https://github.com/ManoMedEngg)
[![Platform](https://img.shields.io/badge/Platform-Mobile%20App-green.svg)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 📌 Project Overview
**GutAngle** is an innovative wearable smart belt system designed to bridge the gap between physical posture and gastrointestinal health. By integrating **Electrogastrography (EGG)** and **Inertial Measurement Unit (IMU)** sensors, the application provides real-time monitoring and biofeedback to improve the user's digestive well-being and spinal alignment.

Developed by **Manoj**, a dedicated Biomedical Engineer, this project leverages signal processing and sensor fusion to provide actionable health insights.

---

## 🚀 Key Features
* **🩺 EGG Signal Monitoring:** Real-time tracking of gastric electrical activity (Bradygastria, Normogastria, and Tachygastria).
* **📏 Posture Analysis:** High-precision IMU data processing to detect slouching or improper seating angles that affect gut motility.
* **📊 Live Data Visualization:** Interactive graphs showing the correlation between body angle and gastric rhythm.
* **⚠️ Smart Alerts:** Haptic or notification-based reminders to correct posture for optimal digestion.
* **📅 Health Logs:** Historical data storage to track improvements over time.

---

## ⚙️ Detailed Workflow
The system operates through a sophisticated pipeline from hardware acquisition to mobile visualization:

### 1. Data Acquisition (Hardware Layer)
* **EGG Electrodes:** Capture micro-volt signals from the abdominal surface.
* **IMU Sensor (MPU6050/BNO055):** Tracks 3-axis acceleration and gyroscopic data to determine the "Gut Angle."

### 2. Signal Processing (Firmware Layer)
* **Filtering:** Digital Band-pass filters (0.01 Hz to 0.5 Hz) to isolate the slow-wave gastric signals.
* **Normalization:** Scaling IMU raw data into readable degrees and orientation vectors.

### 3. Communication (Bluetooth/Wi-Fi)
* Data is packaged and transmitted via BLE (Bluetooth Low Energy) to the **GutAngle Application** to ensure low power consumption for the wearable.

### 4. Analysis & UI (Application Layer)
* The app calculates the relationship between the spine's curvature and gastric efficiency.
* Users receive a **"Digestive Posture Score"** based on real-time data.

---

## 🛠️ Tech Stack
| Component | Technology |
| :--- | :--- |
| **Language** | Python / Flutter (Dart) |
| **Sensors** | EGG Leads, IMU (6-DOF) |
| **Communication** | BLE / MQTT |
| **Processing** | SciPy, NumPy (Signal Analysis) |
| **Design** | Figma (UI/UX) |

---

## 📁 Repository Structure
```text
├── assets/             # Images, Icons (including icon.png)
├── src/                # Source code for the application
├── drivers/            # Sensor interface scripts
├── docs/               # Technical manuals and circuit diagrams
└── README.md           # Project documentation
