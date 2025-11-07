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
* ...and any others you need!

### 2. Create Your Main Screen

Here is a complete example of a main screen with a `Scaffold`. It connects to the service, displays a `LazyColumn` of media items, and shows a `bottomBar` mini-player when the controller is ready.

```kotlin
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun YourMainAppScreen(modifier: Modifier = Modifier) {

    // The magic line: Connects to your service
    // Replace 'PlayerLibrarySessionService' with your app's service
    val mediaController by rememberMediaController<PlayerLibrarySessionService>()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("List Of Media") })
        },
        content = {
            LazyColumn(contentPadding = it) {  
       
                // Use the helper to list items
                items(mediaController?.mediaItems ?: emptyList()) { mediaItem ->
                    ListItem(
                        headlineContent = {
                            Text(mediaItem.mediaMetadata.title.toString() ?: "Unknown Title")
                        },
                        supportingContent = {
                            Text(mediaItem.mediaMetadata.artist.toString() ?: "Unknown Artist")
                        }
                        // Add an onClick to play this item
                        // modifier = Modifier.clickable { mediaController?.play(mediaItem) }
                    )
                }
                
            }
        },
        bottomBar = {
            // Show the mini-player only when the controller is ready
            mediaController?.let { player ->
                YourFullScreenViewOrMiniPlayer(player)
            }
        }
    )
}
```

```kotlin
/**
 * A helper extension to easily get a List<MediaItem> from the Player.
 * Place this in a 'PlayerExtensions.kt' file.
 * You shouldnt ideally don't use this method as you prob get your data from a database and manage your list by yourself
 */
private val Player.mediaItems: List<MediaItem>
    get() = object : AbstractList<MediaItem>() {
        override val size: Int
            get() = mediaItemCount
        override fun get(index: Int): MediaItem = getMediaItemAt(index)
    }
```

The MiniPlayer(that usually sits on the bottom of the screen) or Fullscreen view of your player

```kotlin
@OptIn(UnstableApi::class)
@Composable
fun YourFullScreenViewOrMiniPlayer(player: Player) {

    // --- Default Media3 State Helpers ---
    val playPauseButtonState = rememberPlayPauseButtonState(player)
    val previousButtonState = rememberPreviousButtonState(player)
    val nextButtonState = rememberNextButtonState(player)
    // The default seek buttons (if you want them)
    // val defaultSeekBackButtonState = androidx.media3.ui.compose.state.rememberSeekBackButtonState(player)
    // val defaultSeekForwardButtonState = androidx.media3.ui.compose.state.rememberSeekForwardButtonState(player)

    // --- Custom Utils from this Repo ---

    // These listen for isMediaItemDynamic, allowing seek in live DVR streams
    val seekBackButtonState = rememberSeekBackButtonState(player)
    val seekForwardButtonState = rememberSeekForwardButtonState(player)
    
    // This gives you easy access to metadata
    val mediaMetadataState = rememberMediaMetadata(player)
    // This gives you the current MediaItem object
    val currentMediaItem = rememberCurrentMediaItemState(player)

    // --- Build Your UI ---
    Row {

        AsyncImage(
            model = mediaMetadataState.artworkData ?: mediaMetadataState.artworkUri, 
            contentDescription = "Player Artwork"
        )

        Column {
            Text(mediaMetadataState.title?.toString() ?: "No Title")
            Text(mediaMetadataState.artist?.toString() ?: "Unknown Artist")
            Text(mediaMetadataState.albumTitle?.toString() ?: "Unknown Album")
        }

        Row {
            Button(
                onClick = seekBackButtonState::onClick,
                enabled = seekBackButtonState.isEnabled,
                content = { Text("FR") }
            )
            Button(
                onClick = previousButtonState::onClick,
                enabled = previousButtonState.isEnabled,
                content = { Text("Prev") }
            )
            Button(
                onClick = playPauseButtonState::onClick,
                content = { 
                    Text(if (playPauseButtonState.showPlay) "Play" else "Pause") 
                }
            )
            Button(
                onClick = nextButtonState::onClick,
                enabled = nextButtonState.isEnabled,
                content = { Text("Next") }
            )
            Button(
                onClick = seekForwardButtonState::onClick,
                enabled = seekForwardButtonState.isEnabled,
                content = { Text("FF") }
            )
        }
    }
}
```
