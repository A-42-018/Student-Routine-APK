# Student Routine - Android App

A modern, student-friendly Android app for managing daily routine activities.

## Features
- **Dashboard**: View today's classes and pending tasks at a glance
- **Timetable**: Weekly class schedule with day filtering
- **Tasks**: To-do list with priorities, due dates, and completion tracking
- **Reminders**: Local push notifications for upcoming classes and task deadlines
- **Nearby Libraries**: Find libraries near your current location with map integration
- **Modern UI**: Built with Jetpack Compose using a clean card-based design

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with ViewModel + StateFlow
- **Database**: Room (SQLite)
- **Navigation**: Jetpack Navigation Compose
- **Notifications**: AlarmManager + BroadcastReceiver

## Project Structure
```
app/src/main/java/com/alif/studentroutine/
├── data/
│   ├── database/       # Room Database & DAOs
│   ├── entity/         # Data models (ClassItem, TaskItem)
│   └── repository/     # Data access layer
├── location/           # LocationHelper for GPS access
├── notification/       # AlarmReceiver, NotificationHelper
├── ui/
│   ├── navigation/     # NavGraph & screen routes
│   ├── screens/        # All Composable screens
│   └── theme/          # Colors, Typography, Theme
├── viewmodel/          # Screen ViewModels
├── MainActivity.kt     # Entry point
└── StudentRoutineApp.kt # Application class
```

## How to Build the APK

### Option 1: Android Studio (Recommended)
1. Open Android Studio
2. Select **File → Open** and choose the `My routine` folder
3. Let Gradle sync (it will auto-download dependencies)
4. Connect an Android device or start an emulator
5. Click **Run** (▶) or go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**
6. The APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

### Option 2: Command Line
Make sure you have Java 17+ installed, then run:

```bash
# On macOS/Linux
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

### Option 3: Release APK (Signed)
1. In Android Studio: **Build → Generate Signed Bundle or APK**
2. Select **APK**
3. Create or select a keystore
4. Choose `release` build variant
5. The signed APK will be at:
   `app/build/outputs/apk/release/app-release.apk`

## Permissions Required
- `POST_NOTIFICATIONS` - For reminder notifications
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` - For precise reminder timing
- `RECEIVE_BOOT_COMPLETED` - To reschedule alarms after device reboot
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` - For finding nearby libraries

## Note on Nearby Libraries
The **Nearby Libraries** feature uses your device's GPS location and opens Google Maps to show nearby libraries. For real-time library data from Google Places API, add a Places API key and integrate with the `NearbyLibrariesScreen`. The current version uses sample data for demonstration.

## Minimum Requirements
- Android 8.0 (API 26) or higher
- ~15 MB storage space
