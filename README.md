# media3-compose-utils

This is a collection of files and code that helps you get up and running with a Jetpack Compose Media3 app in no time.

I don't have the understanding of software licenses so treat this text as the current license 
"You are free to use my files and code as you like but I want you to notify me if you change some code and make them better so I can clone the new code and implement it into my files unless you keep the modified files strictly to yourself and don't share them with anyone"
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
---

## 2. The MediaSessionService Setup
if you arent already using a service i highly recommend you to switch, this is the bare minimum you need to make your `PlaybackService` run. This service holds the `ExoPlayer` and the `MediaSession`, allowing background playback and external control.

```kotlin
@UnstableApi
private class PlaybackService : MediaSessionService() {

    private lateinit var mediaSession: MediaSession

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this).build()
            .apply {
                
                // this isn't necessary its just there so you can seek through the playing contents of a station faster
                setSeekBackIncrementMs( 30*1000L )
                setSeekForwardIncrementMs( 30*1000L )
                
                setAudioAttributes(AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true)
                setHandleAudioBecomingNoisy(true)
                setWakeMode(WAKE_MODE_NETWORK)
                
            }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSession.Builder(this, player).build()

        player.run {
            addMediaItems(
                listOf(
                    MediaItem.Builder().setMediaId("1").setUri("[http://nebula.shoutca.st:8545/stream](http://nebula.shoutca.st:8545/stream)").setMediaMetadata(MediaMetadata.Builder().setTitle("ZFM").setArtworkUri("[https://nz.radio.net/300/zfm.png?version=a00d95bdda87861f1584dc30caffb0f9](https://nz.radio.net/300/zfm.png?version=a00d95bdda87861f1584dc30caffb0f9)".toUri()).build()).build(),
                    MediaItem.Builder().setMediaId("2").setUri("[https://live.visir.is/hls-radio/fm957/chunklist_DVR.m3u8](https://live.visir.is/hls-radio/fm957/chunklist_DVR.m3u8)").setMediaMetadata(MediaMetadata.Builder().setTitle("FM 957").setArtworkUri("[https://www.visir.is/mi/300x300/ci/ef50c5c5-6abf-4dfe-910c-04d88b6bdaef.png](https://www.visir.is/mi/300x300/ci/ef50c5c5-6abf-4dfe-910c-04d88b6bdaef.png)".toUri()).build()).build()
                )
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.run {
            release()
            player.stop()
            player.release()
        }
    }
}
```

---

### Your typical `MainActivity` or the class hosting your very root composable
```kotlin
private class YourMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            //you just init your composable as usual no extra ordinary is required for the mediaController to function
            App()
        }
    }
}
```

---

### Your Main `App()` composable 
```kotlin
@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun App() {

    //The magic starts here, this is your bridge between your service and the ui replace "PlayerService" with either an extension of "MediaLibraryService" or a "MediaSessionService"
    val mediaController by rememberMediaController<PlaybackService>()

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
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                model = mediaItem.mediaMetadata.artworkData ?: mediaItem.mediaMetadata.artworkUri,
                                contentDescription = "List Item Artwork"
                            )
                        },
                        headlineContent = {
                            Text(mediaItem.mediaMetadata.title?.toString() ?: "Unknown Title")
                        },
                        supportingContent = mediaItem.mediaMetadata.title?.let { { Text(it.toString()) } }
                    )
                }

            }
        },
        bottomBar = {
            mediaController?.run {
                MiniPlayer({ this })
            }
        }
    )


}
```

---

### The MiniPlayer(PlayBar)
```kotlin
//passing the player as a lambda prevents it from recomposing the MiniPlayer every time something inside the player object itself changes and since we don't observe the player directly this is the right way to do it
@OptIn(UnstableApi::class)
@Composable
private fun MiniPlayer(player: () -> Player) {

    val player = player()

    //common default compose media3 methods and some more
    val playPauseButtonState = rememberPlayPauseButtonState(player)
    val previousButtonState = rememberPreviousButtonState(player)
    val nextButtonState = rememberNextButtonState(player)
    // The default seek buttons (if you want them)
    // val defaultSeekBackButtonState = androidx.media3.ui.compose.state.rememberSeekBackButtonState(player)
    // val defaultSeekForwardButtonState = androidx.media3.ui.compose.state.rememberSeekForwardButtonState(player)

    //common custom compose media3 methods and some more
    // These listen for isMediaItemDynamic, allowing seek in live DVR streams
    val seekBackButtonState = rememberSeekBackButtonState(player)
    val seekForwardButtonState = rememberSeekForwardButtonState(player)

    // This gives you easy access to metadata
    val mediaMetadataState = rememberMediaMetadata(player)

    //This gives you the current MediaItem object
    //a basic version of a mediaItem without buildUpon() and all the other functions just the basics
    //val currentMediaItemState = rememberCurrentMediaItemState(player)
    //if you prefer to get access to the entire mediaItem and all its functions you should use this method instead as it returns a real State<MediaItem?>
    //val currentMediaItem by rememberCurrentMediaItem(player)

    MiniPlayer(
        isPlaying = !playPauseButtonState.showPlay,
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
//here you should ideally also pass everything as lambdas to prevent the entire composable from recomposing but in such small composable it doesn't really matter but its good practice
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
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = {
                //coil.compose.AsyncImage
                AsyncImage(model = artwork, contentDescription = "Player Artwork", modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    ))
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
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            content = {

                onRewind?.let {
                    IconButton(onClick = it, content = { Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Fast Rewind") })
                }

                onPrevious?.let {
                    IconButton(onClick = it, content = { Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous") })
                }

                onTogglePlayback?.let {
                    FilledIconButton(
                        shape = if (isPlaying) IconButtonDefaults.smallSquareShape else IconButtonDefaults.smallRoundShape,
                        onClick = it,
                        content = {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                        }
                    )
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

## A small helper function to get the current playlist from the player
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

## Acknowledgements

The `rememberMediaController` utility was inspired by and adapted from the `rememberManagedMediaController` in the [RadioRoam project](https://github.com/oguzhaneksi/RadioRoam/blob/master/app/src/main/java/com/radioroam/android/ui/components/mediacontroller/MediaController.kt).

This version, developed in collaboration with AI, was extensively refactored to improve reusability and developer experience. Key enhancements include:

* **Generic Service Support:** The original implementation was refactored to be fully generic, removing the hardcoded service dependency. This allows it to connect to *any* `MediaSessionService` provided by the user.
* **Idiomatic Compose API:** An `inline reified` overload (`rememberMediaController<Service>()`) was added for a cleaner, more "Compose-native" call syntax that eliminates boilerplate.
* **Encapsulation:** The connection logic was further encapsulated, simplifying its use and management within any composable.
