# Learning Dashboard

Android app (Kotlin, Jetpack Compose). Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

Log in with `demo@learning.app` / `password123`.

## 1. Architecture

MVVM with a repository. Screens collect a `StateFlow` from a ViewModel. ViewModels call `AuthRepository` or `CourseRepository`. Only the course repository talks to `CourseApi` and Room. `AppContainer` constructs that graph in one place, without a DI framework, so a small app stays easy to follow.

`CourseApi` is an interface. The current implementation reads bundled JSON when `ConnectivityManager` reports a network. Replacing it with Retrofit later changes that one class, not the screens.

## 2. Offline support

Room is the source of truth after the first successful load. A refresh writes courses and lessons, and keeps a lesson's local `completed` flag when that lesson id already exists, so a later refresh does not wipe progress. The dashboard collects the database. If the refresh fails and rows are already stored, those rows stay on screen with a banner and Retry. If it fails and the database is empty, the screen shows an error and Retry. Marking a lesson complete writes only to Room, so it works with the network off.

Progress is `completedLessons * 100 / totalLessons`, and 0 when a course has no lessons. Seeded counts: Python 13/20 = 65%, Full Stack 7/28 = 25%, Generative AI 6/16 = 37% because 40% is not a whole number of 16 lessons.

## 3. Security

This login is mocked and the session stays in memory. In production I would keep the access token in memory and store the refresh token in Encrypted DataStore or EncryptedSharedPreferences, with the key in the Android Keystore. I would not put tokens in plain SharedPreferences, logs, or source control.

## 4. Scale

For 1 million users and hundreds of courses I would: page the course list and load lessons per course instead of sending every lesson in the list payload; sync completions with the backend (WorkManager, plus `updatedAt` or an etag) instead of a device-only flag; split feature modules and add baseline profiles, R8, and crash reporting; serve images and static content from a CDN; and use short-lived access tokens, refresh, and certificate pinning.

## 5. Second platform

On iOS/macOS I would keep the same boundaries in SwiftUI. Views bind to observable view models. A repository coordinates URLSession and SwiftData (or GRDB). `NWPathMonitor` plays the role of `ConnectivityManager`. Lesson completion stays a local write. The same loading, empty, error, and cached-offline states map onto the SwiftUI screens.
