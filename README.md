# Movie Streaming App - Android

Android client for the Kotlin Multiplatform Movie Streaming application.

## Tech Stack

- Kotlin
- Jetpack Compose
- Kotlin Multiplatform (KMP)
- Ktor Client
- Media3 ExoPlayer
- MediaSession
- Coroutines
- Kotlin Serialization

## Features

- Movie list
- Movie details
- Remote video streaming
- Play / Pause / Seek
- Buffering state
- Playback error handling
- Test advertisement with `ADVERTISEMENT` label
- Background playback
- Media controls through MediaSession
- KMP shared business logic and UI

## Android Architecture

```text
Compose UI
    ↓
ViewModel
    ↓
Repository
    ↓
Ktor Client
    ↓
Ktor Backend
```

For video playback:

```text
Compose VideoPlayer
        ↓
MediaController
        ↓
MediaSession
        ↓
PlaybackService
        ↓
ExoPlayer
```

The `PlaybackService` owns the ExoPlayer instance so playback can continue when the Android app goes into the background.

## Main Android Components

### UI
Jetpack Compose is used to build the movie list, movie details and video player screens.

### ViewModel
Manages UI state and requests movie data from the repository.

### Repository
Provides a clean abstraction between the UI/ViewModel and the network layer.

### Ktor Client
Communicates with the Ktor backend to retrieve movie information.

### ExoPlayer
Media3 ExoPlayer is used for remote video streaming and playback controls.

### PlaybackService
A Media3 `MediaSessionService` manages background playback and exposes the player through a `MediaController`.

## Backend URL

For Android Emulator:

```text
http://10.0.2.2:8080
```

For a physical Android device, use the computer's local network IP:

```text
http://YOUR_LOCAL_IP:8080
```

The backend must be running and accessible from the device.

## Background Playback

Android background playback uses:

```text
PlaybackService
    ↓
ExoPlayer
    ↓
MediaSession
    ↓
MediaController
```

The foreground service is configured for:

```text
mediaPlayback
```

This allows the video/audio playback session to continue when the application is moved to the background.

## Run Android App

1. Start the Ktor backend.
2. Configure the Android backend base URL.
3. Open the project in Android Studio.
4. Sync Gradle.
5. Run the Android application on an emulator or physical device.

## Testing

Test the following:

- Movie list loads correctly.
- Movie details load correctly.
- Video starts from a remote URL.
- Play / pause works.
- Seeking works.
- Buffering state is displayed.
- Playback errors are handled.
- Advertisement is displayed with a clear label.
- Pressing Home/backgrounding the app does not stop playback.
- Media controls work from the notification/lock screen.

## Security

No API keys or secrets are stored in the Android application. Movie API access is handled through the backend.

## Requirements

- Android Studio
- Android SDK
- JDK 21+
- Running Ktor backend
