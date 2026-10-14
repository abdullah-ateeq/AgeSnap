# AgeSnap 🎂⏱️

**AgeSnap** is a modern, feature-rich Android application designed for precise age calculation, profile management, and time tracking. Built entirely with **Jetpack Compose** and **Material 3**, AgeSnap provides a sleek, responsive, and delightful user experience for calculating exact ages, tracking upcoming birthdays, viewing detailed age statistics, and managing saved profiles.

---

## 🌟 Key Features

- **Precise Age Calculation**: Instantly compute exact age in years, months, weeks, days, hours, minutes, and seconds from any birthdate.
- **Next Birthday Countdown**: See exactly how many months, days, and hours remain until the next birthday, complete with motivational milestones.
- **Comprehensive Statistics**: View total days lived, total weeks, total hours, and zodiac/astrological signs.
- **Profile Management**: Save, edit, and quickly switch between multiple family and friend profiles.
- **Calculation History**: Keep track of recent calculations with quick-load capabilities.
- **Share & Export**: Easily format and share age summaries with friends and family via system share sheets.
- **Material 3 & Dark Mode**: Dynamic color support, clean typography, and polished animations following Google's Material 3 design guidelines.

---

## 📱 Screenshots & UI

*(Add screenshots here)*

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3.
- **Architecture**: MVVM (Model-View-ViewModel) with unidirectional data flow and `StateFlow`.
- **Asynchronous Operations**: Kotlin Coroutines & Flow.
- **Navigation**: Jetpack Navigation Compose.
- **Testing**: JUnit 4, Kotlin Coroutines Test, and Compose UI testing.
- **Min SDK**: API 26 (Android 8.0+)
- **Compile/Target SDK**: API 37

---

## 📂 Project Structure

```tree
app/src/main/java/com/example/agesnap/
├── data/
│   ├── model/         # Data classes (AgeProfile, AgeResult, UserSettings, etc.)
│   └── repository/    # Data persistence and storage repositories
├── domain/            # Business logic (AgeCalculator, BirthdayCalculator, AgeValidator, etc.)
├── ui/
│   ├── components/    # Reusable UI components (DatePicker, Cards, Dialogs)
│   ├── home/          # Home screen & calculation dashboard
│   ├── profiles/      # Saved profiles management screen
│   ├── settings/      # Settings & preferences screen
│   ├── about/         # About screen
│   ├── navigation/    # Navigation destinations & graph
│   └── theme/         # Material 3 colors, typography & theme
├── viewmodel/         # AgeSnapViewModel & UI state management
└── MainActivity.kt
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Koala or newer.
- JDK 11 or higher.
- Android SDK configured with build tools for API 37.

### Building and Running
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/AgeSnap.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and run the `:app` module on an emulator or physical Android device (API 26+).

---

## 🧪 Running Tests

To execute unit tests:
```bash
./gradlew test
```

To execute instrumented tests:
```bash
./gradlew connectedAndroidTest
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

Developed by **Abdullah Ateeq** ([@abdullah-ateeq](https://github.com/abdullah-ateeq)).
