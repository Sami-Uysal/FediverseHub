# FediverseHub

Android-first Fediverse client built with Kotlin, Jetpack Compose, Material 3, Hilt, Ktor, Room, Paging 3, Coil, DataStore, and Navigation Compose.

## Features

- Multi-account shell for Mastodon, Pixelfed, and Lemmy.
- Mastodon OAuth login, home timeline, post detail, actions, compose, notifications, profile, search, and explore.
- Pixelfed OAuth login, home feed, post detail, likes, comments, profile media grid, and explore grid.
- Lemmy login, subscribed home feed, explore, post detail, nested comments, voting, save, community pages, follow, comments, and post compose.
- Platform-aware bottom tabs and settings.
- Offline Mastodon timeline cache with clear-cache control.
- Android Keystore backed token encryption with legacy DataStore token migration.
- System, light, and dark theme modes.

## Release Checks

- `.\gradlew.bat :app:assembleDebug --no-daemon --console=plain`
- `.\gradlew.bat :app:testDebugUnitTest --no-daemon --console=plain`
- `.\gradlew.bat :app:assembleRelease --no-daemon --console=plain`
