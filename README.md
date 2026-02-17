Here is the updated README.md with the License section and Releases link added.
🎸 Plectune - Guitar Tuner, Metronome & Chords

A high-precision, low-latency musical utility app built with Kotlin, Jetpack Compose, and C++ (Oboe).

Plectune combines professional-grade tuning accuracy with a stunning Liquid Glass UI, bringing the complex physics of iOS-style blur and vibrancy to Android.
📥 Download

Get the latest APK from the Releases Page:
👉 Download Plectune (Latest Release)
✨ Features
🎯 Professional Tuner

    YIN Pitch Detection: Utilizes the industry-standard YIN algorithm for high-precision pitch tracking.

    Living Graph UI: A unique "snake trail" history graph that visualizes pitch stability over time.

    Chromatic & Standard: Accurate detection for guitar and other instruments.

⏱️ Precision Metronome

    C++ Audio Engine: Powered by Google's Oboe library for ultra-low latency timing that won't drift.

    Visual Pulse: A beautiful, wave-based visualizer that syncs perfectly with the beat.

    Liquid Controls: Interactive tap tempo, adjustable time signatures (1/4 to 12/4), and a physics-based BPM slider.

🎼 Extensive Chord Library

    Interactive Charts: Browse chords by root and quality (Major, Minor, 7th, etc.) with a smooth horizontal pager.

    Audio Playback: Hear how every chord sounds with integrated playback.

    Fretboard Visualization: Clear, generated diagrams showing exact finger positions.

🎨 Liquid Glass Design

    iOS-Style Physics: Implements complex blur, vibrancy, and light refraction effects on Android.

    Interactive Components: Buttons and sliders feel organic, reacting to touch pressure and drag with damped spring animations.

📱 Screenshots
Tuner	Metronome	Chords
		
🛠️ Tech Stack

Core

    Language: Kotlin (2.3.10)

    Native Interface: C++20 (JNI) & CMake

    UI Framework: Jetpack Compose (Material 3)

    Min SDK: 30 (Android 11)

    Target SDK: 36 (Android 16)

Audio & Performance

    Oboe: High-performance audio library for low-latency metronome clicks.

    YIN Algorithm: Custom implementation for pitch detection.

Architecture & Libraries

    Architecture: MVI (Model-View-Intent)

    Dependency Injection: Koin

    Navigation: Jetpack Navigation Compose

    UI Effects: kyant0/backdrop (Liquid Glass), kyant0/shapes

📥 Installation

Prerequisites

    Android Studio (Ladybug | 2024.2.1 or later recommended)

    JDK 21

    Android SDK 30+

Steps

    Clone the repository
    Bash

    git clone https://github.com/Quantum3600/Plectune.git
    cd Plectune

    Open in Android Studio

        Select "Open an Existing Project" and navigate to the Plectune directory.

    Build the project
    Bash

    ./gradlew build

    Run on Device

        Ensure your device supports Android 11 (API 30) or higher.

        Connect your instrument and start tuning!

🏗️ Architecture

The app follows a clean MVI (Model-View-Intent) architecture pattern:
Plaintext

📦 com.trishit.plectune
 ┣ 📂 feature
 ┃ ┣ 📂 tuner          # YIN algo, Gauge UI, Tone Generator
 ┃ ┣ 📂 metronome      # Oboe integration, Pulse UI
 ┃ ┗ 📂 chords         # Chord repository, Fretboard views
 ┣ 📂 ui
 ┃ ┣ 📂 components     # LiquidButton, LiquidSlider, Fretboard
 ┃ ┗ 📂 theme          # Type, Color, Theme
 ┗ 📂 di               # Koin Modules

📄 License

This project is licensed under the Apache License, Version 2.0.
Plaintext

Copyright 2026 Trishit Majumdar

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

🙏 Acknowledgments & Credits

    Kyant0: Huge shoutout for the Backdrop (AndroidLiquidGlass) library. This project serves as a comprehensive demo of replicating complex iOS liquid glass physics and blur effects on Android.

    Google Oboe: For enabling low-latency audio features required for the metronome.

    Material Design 3: For the foundational design guidelines.

👤 Author

Trishit Majumdar (@Quantum3600)

    📧 Reach out

    🐙 GitHub

Made with ❤️, ☕, and Kotlin.
