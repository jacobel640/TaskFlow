<div align="center">

<img src="screenshots/app_icon.png" alt="TaskFlow Icon" width="120"/>

# TaskFlow

**A Modern, Clean, and Intuitive Android Task Management Application**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF.svg?logo=kotlin&logoColor=white)](#)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4.svg?logo=android&logoColor=white)](#)
[![Architecture](https://img.shields.io/badge/Architecture-Clean_%2B_MVVM-success.svg)](#)
[![Room](https://img.shields.io/badge/Database-Room-blue.svg)](#)
[![Hilt](https://img.shields.io/badge/DI-Dagger_Hilt-orange.svg)](#)

TaskFlow is built to demonstrate modern Android development best practices, featuring a fully declarative UI, robust local storage, background processing, and a highly scalable architecture.

[Explore Features](#-features) • [View Screenshots](#-screenshots) • [Tech Stack](#-tech-stack) • [Getting Started](#-getting-started)

</div>

---

## ✨ Features

### 🎯 Core Task Management
* **Create & Edit:** Seamlessly add, update, and categorize tasks.
* **Quick Swipe Actions:** Easily swipe on any task to instantly toggle its status between **Completed** and **TODO**.
* **Smart Search & Filtering:** Find tasks instantly using the real-time search bar, or filter them by **Status** (Done, Pending) and **Priority** (High, Medium, Low).
* **Sorting Options:** Keep things organized by sorting tasks by Date or Title.
* **Undo/Redo:** Built-in history management when editing tasks to prevent accidental data loss.

### 📅 Advanced Calendar Views
* Navigate your schedule with dynamic calendar integration.
* Switch effortlessly between **Daily**, **Weekly**, **Monthly**, and **Yearly** perspectives.

### 🔔 Background & System Integrations
* **Daily Notifications:** Never miss a task! Powered by `WorkManager` and `AlarmManager` for reliable, scheduled reminders. You can configure the exact notification time.
* **Boot Receiver:** Automatically reschedules notifications when the device reboots.
* **User Preferences:** Saves user settings (like notification times and sorting preferences) safely using `DataStore Preferences`.

### 🎨 Modern UI/UX
* **Material Design 3:** Beautiful, responsive, and intuitive interface.
* **Dark/Light Theme:** Full support for system-wide dark and light modes.

---

## 📸 Screenshots

<div align="center">
  <table>
    <tr>
      <td align="center"><b>Home - Sorting</b></td>
      <td align="center"><b>Home - Filters</b></td>
      <td align="center"><b>Swipe Actions & Undo</b></td>
      <td align="center"><b>Task Details</b></td>
    </tr>
    <tr>
      <td><img src="screenshots/HomeScreen_sorting.png" width="220" alt="Home Screen Sorting"/></td>
      <td><img src="screenshots/HomeScreen_filters.png" width="220" alt="Home Screen Filters"/></td>
      <td><img src="screenshots/HomeScreen_mark-completed_undo-deletion.png" width="220" alt="Home Screen Swipe Actions"/></td>
      <td><img src="screenshots/SingleTaskScreen.png" width="220" alt="Single Task Screen"/></td>
    </tr>
    <tr>
      <td align="center"><b>Add/Edit Task</b></td>
      <td align="center"><b>Calendar - Month View</b></td>
      <td align="center"><b>Calendar - Day View</b></td>
      <td align="center"><b>Notification Settings</b></td>
    </tr>
    <tr>
      <td><img src="screenshots/AddEditTaskScreen.png" width="220" alt="Add or Edit Task"/></td>
      <td><img src="screenshots/CalendarScreen_month.png" width="220" alt="Calendar Month View"/></td>
      <td><img src="screenshots/CalendarScreen_day.png" width="220" alt="Calendar Day View"/></td>
      <td><img src="screenshots/NotificationTimeDialog.png" width="220" alt="Notification Time Dialog"/></td>
    </tr>
  </table>
</div>

---

## 🛠 Tech Stack & Architecture

This project strictly follows **Clean Architecture** principles (Data, Domain, and Presentation layers) along with the **Unidirectional Data Flow (UDF)** and **MVVM** patterns.

### UI & Presentation
* **Jetpack Compose:** For building a declarative and reactive UI.
* **Material 3:** For modern UI components and theming.
* **Compose Navigation:** Type-safe navigation between screens.
* **ViewModel:** Lifecycle-aware state management.

### Domain & Business Logic
* **Kotlin Coroutines & Flows:** For asynchronous programming and reactive data streams.
* **Use Cases:** Encapsulating specific business rules (e.g., `GetFilteredTasks`, `InsertTask`).

### Data & Local Storage
* **Room Database:** Local SQLite database mapping for offline task storage.
* **DataStore Preferences:** Asynchronous preference saving (replacing SharedPreferences).
* **Dagger Hilt:** Dependency Injection to ensure components are decoupled and testable.

### Background Processing
* **WorkManager:** For deferrable, guaranteed background work (e.g., DailyTaskWorker).
* **Broadcast Receivers:** For handling system events like boot completed and alarms.

---

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites
* **Android Studio** (Koala or newer recommended)
* **JDK 17**
* Minimum Android SDK: **API 29** (Android 10)

### Installation
1. Clone the repository:
   ```bash
   git clone [https://github.com/jacobel640/taskflow.git](https://github.com/jacobel640/taskflow.git)
   ```
2. Open the project in **Android Studio**.
3. Let Gradle complete the sync and download all necessary dependencies.
4. Build and run the app on an emulator or a physical Android device.

---

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
