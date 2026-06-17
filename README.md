<h1 align="center">🚀 Real-Time Video Streaming (AeroFeed)</h1>

<p align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=kotlin,androidstudio,gradle,python" />
  </a>
</p>

---

## 📌 Overview

<table align="center">
  <tr>
    <td><!-- PLACEHOLDER FOR APP DEMO VIDEO --></td>
  </tr>
</table>

AeroFeed is a real-time video streaming application that transmits a live camera feed by utilizing hardware-accelerated H.264 video encoding via MediaCodec and Camera2 API to stream directly using the GPU, reducing the CPU load, to a desktop client over a local network. The video is transmitted using the RTSP real-time streaming protocol on port 8554 for continuous streaming.

---

## 📊 Performance Results
* **Video Quality:** 720p resolution at 30 FPS.
* **Latency (Delay):** Around 200ms to 500ms delay on a normal Wi-Fi network.
* **CPU Usage:** The app uses MediaCodec and Camera2 API to do video encoding on the GPU. This keeps the CPU free and prevents lags or fast battery drain.

---

## ✨ Features

| Feature | Description |
|---|---|
| 📡 **Hardware H.264 Encoding** | Uses the Camera2 API and MediaCodec for GPU-accelerated video streaming to reduce CPU load. |
| 🛡️ **Foreground Service** | Implemented the RTSP server as a foreground service with a notification. |
| ⚡ **Low-Latency Receiver** | Uses a Python, OpenCV, and Tkinter desktop application to display the live stream. |
| 🏛️ **Clean Architecture** | Built using MVVM design pattern and Clean Architecture principles with Jetpack Compose. |

---

## 🚀 How to Run

### Part 1: Android Application
1. Open the project in **Android Studio**.
2. Sync the Gradle files.
3. Connect a physical Android device (API 24 or above) and click **Run**.
4. Grant the requested **Camera** and **Microphone** permissions.
5. Tap the green **Play** button to start the stream. The app will display your stream URL (e.g., `rtsp://192.168.1.50:8554/live`).

### Part 2: Desktop Receiver
1. Ensure both your PC and Android device are connected to the **same Wi-Fi network**.
2. Install the required Python dependencies:
   ```cmd
   pip install opencv-python pillow
   ```
3. Run the script from your command prompt:
   ```cmd
   python receiver.py
   ```
4. Enter the exact **RTSP URL** displayed on the Android app into the desktop application and click **Play Stream**.

---

##  Assumptions & Limitations
* **Hardware:** The Android phone must support H.264 video encoding.
* **Port Availability:** The app uses port 8554 for streaming. This port must be free and not blocked by the computer's firewall.
* **Manual IP Entry:** The Python desktop app does not find the phone automatically. You have to look at the IP address on the Android app and type it manually.

---

## 🏛️ Architecture & Project Structure

This project uses the MVVM design pattern. The data layer (streaming logic) is separated from the presentation (UI) layer.
```text
AeroFeed/
├── app/src/main/java/com/example/aerofeed/
│   ├── App.kt                      <-- Application startup and Hilt context
│   ├── MainActivity.kt             <-- App launcher and Activity entry point
│   ├── data/service/
│   │   └── RtspService.kt          <-- RTSP server and Camera capture (Data Layer)
│   └── presentation/
│       ├── CameraScreen.kt         <-- Permission request & service binder (UI Layer)
│       ├── CameraPreviewContent.kt <-- OpenGL video preview & UI overlays (UI Layer)
│       └── CameraViewModel.kt      <-- State coordinator and IP scanner (ViewModel Layer)
└── receiver.py                     <-- Low-latency Python GCS player (PC Client)
```

* **Foreground Service:** The RTSP server runs inside a Foreground Service. This prevents the Android OS from killing the stream when the app goes into the background.
* **StateFlow:** The UI automatically updates its Play/Stop buttons and URL text by observing Kotlin StateFlow from the ViewModel.
* **Dependency Injection:** Used Dagger Hilt to inject necessary components cleanly without memory leaks.
