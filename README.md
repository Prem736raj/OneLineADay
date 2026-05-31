# One Line A Day 📝

A beautiful, minimalist micro-journal app where you write just ONE sentence about your day. Zero friction, all local storage, with a stunning timeline view.

## ✨ Features

| Feature | Description |
|---------|-------------|
| **📝 Daily Entry** | Write one line about your day in 10 seconds |
| **😊 Mood Selection** | 10 animated mood emojis to capture how you feel |
| **📷 Photo Attachment** | Add optional photos to your entries |
| **📅 Timeline View** | Beautiful scrollable history grouped by month |
| **🔍 Search** | Search through all your entries |
| **🔥 Streak Tracking** | Current & longest streak with animations |
| **📊 Statistics** | Journey stats, mood distribution, milestones |
| **🏆 Milestones** | Achievements at 7, 30, 100, 365, 1000 entries |
| **📤 PDF Export** | Export journal to shareable document |
| **🔔 Daily Reminders** | Gentle notification to write your line |
| **📱 Home Widget** | See today's entry at a glance |
| **🌙 Dark Mode** | Toggle between light/dark themes |
| **💾 100% Local** | All data stored on device with Room DB |

## 🎨 Design

- **Sunset Color Palette**: Warm amber → rose gradient
- **Premium Typography**: Outfit (headers) + Inter (body) via Google Fonts
- **Animations**: Bouncy mood selection, rotating streak flame, smooth transitions
- **Glassmorphism**: Subtle card shadows and depth
- **Edge-to-Edge**: Full screen utilization

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Installation

1. **Open in Android Studio**
   - Launch Android Studio
   - Select `File > Open`
   - Navigate to this project folder
   - Click `OK` and wait for Gradle sync

2. **Run the App**
   - Connect your Android device (or start an emulator)
   - Click the green **Run** button (▶️)
   - Select your device

3. **Build APK**
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be in `app/build/outputs/apk/debug/`

## 📱 App Structure

```
app/src/main/
├── java/com/onelineaday/journal/
│   ├── MainActivity.kt
│   ├── OneLineApp.kt
│   ├── data/
│   │   ├── JournalEntry.kt
│   │   ├── JournalDao.kt
│   │   ├── JournalDatabase.kt
│   │   └── JournalRepository.kt
│   ├── viewmodel/
│   │   └── JournalViewModel.kt
│   ├── notifications/
│   │   └── ReminderManager.kt
│   ├── widget/
│   │   └── JournalWidget.kt
│   └── ui/
│       ├── theme/
│       │   ├── Color.kt
│       │   ├── Type.kt
│       │   └── Theme.kt
│       ├── components/
│       │   ├── MoodPicker.kt
│       │   ├── EntryCard.kt
│       │   ├── PhotoAttachment.kt
│       │   └── StatsCards.kt
│       └── screens/
│           ├── MainNavigation.kt
│           ├── HomeScreen.kt
│           ├── TimelineScreen.kt
│           ├── StatsScreen.kt
│           └── SettingsScreen.kt
└── res/
    ├── values/
    │   ├── strings.xml
    │   ├── themes.xml
    │   └── font_certs.xml
    ├── drawable/
    │   ├── ic_launcher_foreground.xml
    │   ├── ic_launcher_background.xml
    │   └── widget_background.xml
    ├── layout/
    │   └── widget_journal.xml
    └── xml/
        ├── widget_info.xml
        ├── file_paths.xml
        ├── backup_rules.xml
        └── data_extraction_rules.xml
```

## 🔧 Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material3
- **Architecture**: MVVM
- **Database**: Room
- **Navigation**: Compose Navigation
- **Image Loading**: Coil
- **PDF Export**: iText7
- **Fonts**: Google Fonts (Outfit, Inter)

## 📲 Screens

1. **Home** - Write your daily line, pick mood, attach photo
2. **Timeline** - Browse all entries, search, delete
3. **Journey** - View stats, streaks, mood distribution, milestones
4. **Settings** - Dark mode, notifications, export, widget refresh

## 🔔 Permissions

- `POST_NOTIFICATIONS` - Daily reminder notifications
- `READ_MEDIA_IMAGES` - Photo attachments
- `RECEIVE_BOOT_COMPLETED` - Reschedule notifications after restart
- `SCHEDULE_EXACT_ALARM` - Daily reminder timing

## 📄 License

This project is open source. Use it however you like!

---

Made with ❤️ for capturing life, one line at a time.
