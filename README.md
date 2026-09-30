# Vyxel Launcher

iOS-inspired **Liquid Glass** Android home with a full core launcher stack.

Open in Android Studio, run on a device or emulator, then set **Vyxel Launcher** as the default Home app.

## Core system

- Custom home screen with multi-page grid
- iOS-style glass dock
- Advanced app drawer (vertical, paged, A–Z categories)
- Universal Spotlight search
- Widgets (system AppWidget host + clock/weather)
- Notification shade (Notification Listener)
- Gesture engine (swipe, double-tap, pinch, two-finger)
- Icon packs (`appfilter.xml` / Nova / ADW / GO)
- Per-app icon overrides via layout backup
- Icon shapes (squircle, circle, rounded, teardrop, hex, leaf)
- Dynamic theming + wallpaper palette
- Wallpaper engine (custom image, dim, parallax)
- Folders (drop / long-press)
- App shortcuts (long-press)
- Hidden apps
- Grid + dock customization
- Animation engine (spring / fade / slide / scale)
- Blur and glass opacity
- Custom fonts
- Clock + weather (Open-Meteo)
- Backup / restore / import / export layouts

## Gestures (defaults)

| Gesture | Action |
| --- | --- |
| Swipe up | App drawer |
| Swipe down | Spotlight search |
| Fast swipe down | Notification shade |
| Double tap | Lock screen (device admin) |
| Pinch in | Settings |
| Long press home | Edit mode (widgets / pages / settings) |

## Tech

- Kotlin + Jetpack Compose + Material 3
- minSdk 26, targetSdk 35
- kotlinx.serialization layout/settings JSON
- HOME category launcher

## Build

```bash
# Android Studio: Open this folder, run app
# CI: gradle assembleRelease (see .github/workflows)
```

Version **1.0.0**.
