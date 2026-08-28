# 🏋️‍♂️ GymKo - Offline-First Workout Tracker

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVI-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**GymKo** je moderna Android aplikacija za praćenje treninga i napretka u teretani. Dizajnirana je po *offline-first* principu, što znači da svi podaci ostaju lokalno na uređaju, bez potrebe za internetom ili obaveznom registracijom.

---

## ✨ Key Features

* 📝 **Custom Workout Logging:** Brzo bilježenje serija, ponavljanja i kilaža tijekom treninga.
* 📊 **Progress Tracking:** Vizualni pregled napretka po vježbama i mišićnim skupinama.
* ⏱️ **Rest Timer:** Integrirani tajmer za pauze između serija koji radi u pozadini.
* 🔒 **100% Privacy & Offline:** Svi podaci se spremaju isključivo lokalno na uređaju u Room bazi.
* 🎨 **Modern UI/UX:** Izrađeno u potpunosti pomoću Jetpack Compose-a po Material 3 smjernicama.

---

## 🧪 Try the Alpha Release

Aplikaciji možeš pristupiti putem Google Play Alpha programa ili izravno preuzeti APK:

* 🌐 **Web prijava (Alpha Test):** [Prijavi se putem Weba](https://play.google.com/apps/testing/com.miky.gymko)
* 📱 **Android prijava (Google Play):** [Preuzmi na Google Play-u](https://play.google.com/store/apps/details?id=com.miky.gymko)
* 📦 **Direct APK:** Preuzmi najnoviji `GymKo.apk` sa [GitHub Releases](../../releases/tag/v1.5.0) stranice.

---

## 🛠️ Tech Stack & Architecture

Aplikacija je građena prateći **Modern Android Development (MAD)** preporuke:

* **Language:** [Kotlin](https://kotlinlang.org/)
* **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Architecture:** MVI (Model-View-Intent) / Unidirectional Data Flow (UDF)
* **Local Database:** [Room Database](https://developer.android.com/training/data-storage/room)
* **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
* **Asynchronous & Async Flows:** Kotlin Coroutines & `StateFlow` / `SharedFlow`
* **Navigation:** Jetpack Compose Navigation

---

## 🚀 How to Run locally

1. Kloniraj repozitorij:
   ```bash
   git clone [https://github.com/TvojUsername/GymKo.git](https://github.com/TvojUsername/GymKo.git)

2. Otvori projekt u Android Studio
3. Pokreni aplikaciju na emulatoru ili fizičkom Android uređaju.
