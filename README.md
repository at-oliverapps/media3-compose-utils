# media3-compose-utils

This is a collection of files and code that helps you get up and running with a Jetpack Compose Media3 app in no time.

![License](https://img.shields.io/badge/license-Apache_2.0-blue.svg) ![Platform](https://img.shields.io/badge/platform-Android-brightgreen.svg)

---

## 🤔 What This Solves

The official `media3-ui-compose` library provides great state helpers like `rememberPlayPauseButtonState(player)`, but it leaves you with one big question:

**How do you get the `player` object from your `MediaSessionService` in a clean, composable way?**

This usually requires a lot of boilerplate code (managing `SessionToken`, `ListenableFuture`, and `DisposableEffect`) in your Activity or ViewModel. These utilities solve that problem by providing a single, clean function to handle the connection.

## ✨ Features

* **`rememberMediaController`:** A one-line Composable function to safely connect to your `MediaSessionService` and get a `Player` instance.
* **`rememberMediaMetadata`:** A state holder that gives you direct access to `metadata.title`, `metadata.artist`, etc., and automatically updates your UI.
* **"Smart" Seek Buttons:** Custom `rememberSeekBackButtonState` and `rememberSeekForwardButtonState` that correctly handle seeking in **Live/DVR streams** (which the default Media3 functions do not).
* **`rememberCurrentMediaItemState`:** A simple state holder for the current `MediaItem`.

---

## 🚀 Quick Start

### 1. Add the Files

Since this isn't a hosted library yet, just copy these files into your project's `utils` or `player` package:

* `MediaControllerManager.kt`
* `rememberMediaController.kt`
* `rememberMediaMetadata.kt`
* `rememberSeekBackButtonState.kt`
* `rememberSeekForwardButtonState.kt`
* `rememberCurrentMediaItemState.kt`

*(Don't forget to make the classes/functions **public** if you're putting them in a separate module!)*

### 2. Connect to Your Service

In your main Composable (like `MainActivity` or `YourMainAppScreen`), call `rememberMediaController` with your `MediaSessionService` class as the generic type. This gives you a `State<MediaController?>`.

```kotlin
import io.oliverapps.media3.compose.utils.rememberMediaController
import android.annotation.SuppressLint

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun YourMainAppScreen(modifier: Modifier = Modifier) {
    
    // The magic line: Connects to your service
    // Replace 'YourMediaSessionService' with your app's service
    val mediaController by rememberMediaController<YourMediaSessionService>()
    
    // Show your player UI when the controller is connected
    mediaController?.let { player ->
        // Pass the player to your UI
        YourMiniPlayer(player = player)
    }
    
}
