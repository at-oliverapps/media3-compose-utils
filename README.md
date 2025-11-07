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

* `CurrentMediaItemState.kt`
* `MediaControllerState.kt`
* `MediaMetadataState.kt`
* `SeekBackButtonState.kt`
* `SeekForwardButtonState.kt`
* ...and any others you need!

### 2. Create Your Main Screen

Here is a complete example of a main screen with a `Scaffold`. It connects to the service, displays a `LazyColumn` of media items, and shows a `bottomBar` mini-player when the controller is ready.

```kotlin
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun YourMainAppScreen(modifier: Modifier = Modifier) {

    // The magic line: Connects to your service
    // Replace 'MediaSessionService' with your app's 'MediaSessionService' or 'MediaLibraryService' extension
    val mediaController by rememberMediaController<MediaSessionService>()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("List Of Media") })
        },
        content = {
            LazyColumn(contentPadding = it) {

                itemsIndexed(mediaController?.mediaItems ?: emptyList()) { index, mediaItem ->
                    ListItem(
                        modifier = Modifier.clickable { 
                            //ideally you would use, mediaController?.seekToDefaultPosition(mediaController?.mediaItems?.indexOfFirst { it.mediaId == mediaItem.mediaId } ?: 0), as its more error proof and doesn't work with indexes 
                            mediaController?.seekToDefaultPosition(index)
                        },
                        headlineContent = {
                            Text(mediaItem.mediaMetadata.title.toString() ?: "Unknown Title")
                        },
                        supportingContent = {
                            Text(mediaItem.mediaMetadata.artist.toString() ?: "Unknown Artist")
                        }
                    )
                }

            }
        },
        bottomBar = {
            // this should be enough to get a playbar up and running
            mediaController?.let {
                YourFullScreenViewOrMiniPlayer(it)
            }

            //but in some rare circumstances it has to be initialised like this 
            mediaController?.let { player ->
                val currentMediaItem = rememberCurrentMediaItemState(player)
                currentMediaItem.run {
                    YourFullScreenViewOrMiniPlayer(player)
                }
            }
        }
    )
}
```

```kotlin
/**
 * A helper extension to easily get a List<MediaItem> from the Player.
 * Place this in a 'PlayerExtensions.kt' file.
 *
 * You shouldn't ideally use this method as you prob get your data from a database
 * and manage your list by yourself and for my use this method seems unstable
 * but its good if you just want your app up and running 
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

        //Coil AsyncImage
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

## Acknowledgements

The `rememberMediaController` utility was inspired by and adapted from the `rememberManagedMediaController` in the [RadioRoam project](https://github.com/oguzhaneksi/RadioRoam/blob/master/app/src/main/java/com/radioroam/android/ui/components/mediacontroller/MediaController.kt).

This version, developed in collaboration with AI, was extensively refactored to improve reusability and developer experience. Key enhancements include:

* **Generic Service Support:** The original implementation was refactored to be fully generic, removing the hardcoded service dependency. This allows it to connect to *any* `MediaSessionService` provided by the user.
* **Idiomatic Compose API:** An `inline reified` overload (`rememberMediaController<Service>()`) was added for a cleaner, more "Compose-native" call syntax that eliminates boilerplate.
* **Encapsulation:** The connection logic was further encapsulated, simplifying its use and management within any composable.
