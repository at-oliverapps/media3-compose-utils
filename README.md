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
private fun YourMainAppScreen(modifier: Modifier = Modifier) {

    //The magic starts here , this is your bridge between your service and the ui replace "PlayerService" with either an extension of "MediaLibraryService" or a "MediaSessionService"
    val mediaController by rememberMediaController<PlayerService>()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("List Of Media") })
        },
        content = {
            LazyColumn(contentPadding = it) {

                itemsIndexed(mediaController?.mediaItems ?: emptyList()) { index, mediaItem ->
                    ListItem(
                        modifier = Modifier.clickable {
                            /*
                            ideally you would use
                            mediaController?.run {
                                clearMediaItems()
                                addMediaItems(mediaItems)
                                val index = mediaItems.indexOfFirst { it.mediaId == mediaItem.mediaId }
                                if (index != -1) {
                                    seekToDefaultPosition(index)
                                    play()
                                }
                            }
                            as its more error proof and doesn't work with indexes
                            */

                            //but for this simple example we use
                            mediaController?.seekToDefaultPosition(index)
                        },
                        leadingContent = {
                            //coil.compose.AsyncImage
                            AsyncImage(
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                                model = mediaItem.mediaMetadata.artworkData ?: mediaItem.mediaMetadata.artworkUri,
                                contentDescription = "List Item Artwork"
                            )
                        },
                        headlineContent = {
                            Text(mediaItem.mediaMetadata.title?.let { it.toString() } ?: "Unknown Title")
                        },
                        supportingContent = {
                            Text(mediaItem.mediaMetadata.artist?.let { it.toString() } ?: "Unknown Artist")
                        }
                    )
                }

            }
        },
        bottomBar = {

            mediaController?.run {
                MiniPlayer(this)
            }

            //but in some rare circumstances it has to be initialised like this
            /*
            mediaController?.let { player ->
                val currentMediaItem = rememberCurrentMediaItemState(player)
                currentMediaItem.run {
                    MiniPlayer(player)
                }
            }
            */
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
//Combining all the functions
@OptIn(UnstableApi::class)
@Composable
private fun MiniPlayer(player: Player) {

    //common default compose media3 methods and some more
    val playPauseButtonState = rememberPlayPauseButtonState(player)
    val previousButtonState = rememberPreviousButtonState(player)
    val nextButtonState = rememberNextButtonState(player)
    // The default seek buttons (if you want them)
    // val defaultSeekBackButtonState = androidx.media3.ui.compose.state.rememberSeekBackButtonState(player)
    // val defaultSeekForwardButtonState = androidx.media3.ui.compose.state.rememberSeekForwardButtonState(player)


    // These listen for isMediaItemDynamic, allowing seek in live DVR streams
    val seekBackButtonState = rememberSeekBackButtonState(player)
    val seekForwardButtonState = rememberSeekForwardButtonState(player)

    // This gives you easy access to metadata
    val mediaMetadataState = rememberMediaMetadata(player)

    // This gives you the current MediaItem object
    val currentMediaItem = rememberCurrentMediaItemState(player)

    MiniPlayer(
        isPlaying = playPauseButtonState.showPlay,
        artwork = mediaMetadataState.artworkData ?: mediaMetadataState.artworkUri,
        title = mediaMetadataState.title,
        artist = mediaMetadataState.artist,
        album = mediaMetadataState.albumTitle,
        onRewind = if (seekBackButtonState.isEnabled) seekBackButtonState::onClick else null,
        onPrevious = if (previousButtonState.isEnabled) previousButtonState::onClick else null,
        onTogglePlayback = if (playPauseButtonState.isEnabled) playPauseButtonState::onClick else null,
        onNext = if (nextButtonState.isEnabled) nextButtonState::onClick else null,
        onForward = if (seekForwardButtonState.isEnabled) seekForwardButtonState::onClick else null,
    )

}
```

```kotlin
//Render the ui
@Composable
private fun MiniPlayer(
    //Basic
    isPlaying: Boolean,

    //Basic Metadata
    artwork: Any?,
    title: CharSequence?,
    artist: CharSequence?,
    album: CharSequence?,

    //Basic Buttons
    onRewind: (() -> Unit)?,
    onPrevious: (() -> Unit)?,
    onTogglePlayback: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onForward: (() -> Unit)?,
) = Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
    Column {

        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = {
                //coil.compose.AsyncImage
                AsyncImage(model = artwork, contentDescription = "Player Artwork", modifier = Modifier.size(40.dp).background(
                    MaterialTheme.colorScheme.surfaceVariant))
                Column(modifier = Modifier.weight(1f)) {
                    title?.let {
                        Text(it.toString(), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        artist?.let {
                            Text(it.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        album?.let {
                            Text(it.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                }
            }
        )

        HorizontalDivider()

        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            content = {

                onRewind?.let {
                    IconButton(onClick = it, content = { Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Fast Rewind") })
                }

                onPrevious?.let {
                    IconButton(onClick = it, content = { Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous") })
                }

                onTogglePlayback?.let {
                    IconButton(onClick = it, content = { Icon(imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = if (isPlaying) "Play" else "Pause") })
                }

                onNext?.let {
                    IconButton(onClick = it, content = { Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next") })
                }

                onForward?.let {
                    IconButton(onClick = it, content = { Icon(imageVector = Icons.Default.FastForward, contentDescription = "Fast Forward") })
                }

            }
        )

    }
}
```

## Acknowledgements

The `rememberMediaController` utility was inspired by and adapted from the `rememberManagedMediaController` in the [RadioRoam project](https://github.com/oguzhaneksi/RadioRoam/blob/master/app/src/main/java/com/radioroam/android/ui/components/mediacontroller/MediaController.kt).

This version, developed in collaboration with AI, was extensively refactored to improve reusability and developer experience. Key enhancements include:

* **Generic Service Support:** The original implementation was refactored to be fully generic, removing the hardcoded service dependency. This allows it to connect to *any* `MediaSessionService` provided by the user.
* **Idiomatic Compose API:** An `inline reified` overload (`rememberMediaController<Service>()`) was added for a cleaner, more "Compose-native" call syntax that eliminates boilerplate.
* **Encapsulation:** The connection logic was further encapsulated, simplifying its use and management within any composable.
