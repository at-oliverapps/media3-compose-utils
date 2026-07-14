# media3-compose-utils

This is a collection of files and code that helps you get up and running with a Jetpack Compose Media3 app in no time.

I don't have the understanding of software licenses so treat this text as the current license 
"You are free to use my files and code as you like but I want you to notify me if you change some code and make them better so I can clone the new code and implement it into my files unless you keep the modified files strictly to yourself and don't share them with anyone"
![License](https://img.shields.io/badge/license-Apache_2.0-blue.svg) ![Platform](https://img.shields.io/badge/platform-Android-brightgreen.svg)

---

## it's now a repo grab it here implementation("io.oliverapps/media3-session-compose:1.10.1")

for now you will have to add jitpack to your root build.gradle 

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		maven { url 'https://jitpack.io' }
	}
}

## 🤔 What This Solves

The official `media3-ui-compose` library provides great state helpers like `rememberPlayPauseButtonState(player)` or a bunch of other convinience methods, but it leaves you with one big question:

**How do you get the `player` object from your `MediaSessionService` in a clean, composable way?**

This usually requires a lot of boilerplate code (managing `SessionToken`, `ListenableFuture`, and `DisposableEffect`) in your Activity or ViewModel. These utilities solve that problem by providing a single, clean function to handle the connection.

## ✨ Features

* **`rememberMediaController`:** A one-line Composable function to safely connect to your `MediaSessionService` and get a `Player` instance.
* **"Smart" Seek Buttons:** Custom `rememberSeekBackButtonState` and `rememberSeekForwardButtonState` that correctly handle seeking in **Live/DVR streams** (which the default Media3 functions do not).

---

## 🚀 Quick Start

### 1. Add the Files

Since this isn't a hosted library yet, just copy these files into your project's `utils` or `player` package:

* `CurrentMediaItemState.kt` use the new official `androidx.media3.ui.compose.state.rememberCurrentMediaItemState.mediaItem` instead
* `MediaControllerState.kt`
* `MediaMetadataState.kt` use the new official `androidx.media3.ui.compose.state.rememberCurrentMediaItemState.mediaMetadata` instead
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {

    //The magic starts here, this is your bridge between your service and the ui replace "PlayerService" with either an extension of "MediaLibraryService" or a "MediaSessionService"
    val mediaController by rememberMediaController<PlaybackService>()

    // as media3 have adapted their components to use "Player?" instead of "Player" we don't need to wrap the entire app in a ?.let{} anymore
    Surface {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text(stringResource(shared.string.media_item_list)) })
            },
            content = { padding ->
                MediaItemList(paddingValues = padding, player = { mediaController })
            },
            bottomBar = {
                MiniPlayer(
                    modifier = Modifier.padding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues()),
                    player = { mediaController }
                )
            }
        )
    }

}
```

---

### The MiniPlayer(PlayBar)
```kotlin

//passing the player as a lambda prevents it from recomposing the MiniPlayer every time something inside the player object itself changes and since we don't observe the player directly this is the right way to do it
@OptIn(UnstableApi::class)
@Composable
fun MiniPlayer(
    modifier: Modifier= Modifier,
    player: () -> Player?
) {

    //we are just converting the lambda to a non lambda for convinience
    val player = player()

    //the methods to control
    val playPauseButtonState = rememberPlayPauseButtonState(player)
    val previousButtonState = rememberPreviousButtonState(player)
    val nextButtonState = rememberNextButtonState(player)
    val seekBackButtonState = rememberSeekBackButtonState(player)
    val seekForwardButtonState = rememberSeekForwardButtonState(player)

    //and retrieve metadata
    val mediaItemState = rememberCurrentMediaItemState(player)
    val mediaMetadataState = mediaItemState.mediaMetadata

    MiniPlayer(
        modifier = modifier,
        isPlaying = !playPauseButtonState.showPlay,
        artwork = mediaMetadataState.artworkData ?: mediaMetadataState.artworkUri,
        title = mediaMetadataState.title ?: mediaMetadataState.station,
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
@kotlin.OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MiniPlayer(
    modifier: Modifier = Modifier,

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
    Column(modifier = modifier) {

        //Metadata
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = {
                //coil.compose.AsyncImage
                AsyncImage(model = artwork, contentDescription = "Player Artwork", modifier = Modifier
                    .size(48.dp)
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

        //Controls
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            content = {

                onRewind?.let {
                    IconButton(
                        onClick = it,
                        content = {
                            Icon(imageVector = Constants.Mobile.Icon.FastRewind, contentDescription = stringResource(Constants.Mobile.String.FastRewind))
                        }
                    )
                } ?: Box(modifier = Modifier.minimumInteractiveComponentSize())

                onPrevious?.let {
                    IconButton(
                        onClick = it,
                        content = {
                            Icon(imageVector = Constants.Mobile.Icon.Previous, contentDescription = stringResource(Constants.Mobile.String.Previous))
                        }
                    )
                } ?: Box(modifier = Modifier.minimumInteractiveComponentSize())

                onTogglePlayback?.let {
                    FilledIconButton(
                        shape = if (isPlaying) IconButtonDefaults.smallSquareShape else IconButtonDefaults.smallRoundShape,
                        onClick = it,
                        content = {
                            Icon(imageVector = isPlaying.PlayPauseIcon, contentDescription = stringResource(isPlaying.PlayPauseContentDescription))
                        }
                    )
                } ?: Box(modifier = Modifier.minimumInteractiveComponentSize())

                onNext?.let {
                    IconButton(
                        onClick = it,
                        content = {
                            Icon(imageVector = Constants.Mobile.Icon.Next, contentDescription = stringResource(Constants.Mobile.String.Next))
                        }
                    )
                } ?: Box(modifier = Modifier.minimumInteractiveComponentSize())

                onForward?.let {
                    IconButton(
                        onClick = it,
                        content = { 
                            Icon(imageVector = Constants.Mobile.Icon.FastForward, contentDescription = stringResource(Constants.Mobile.String.FastForward))
                        }
                    )
                } ?: Box(modifier = Modifier.minimumInteractiveComponentSize())

            }
        )

    }
}
```

## A small helper function to get the current playlist from the player
```kotlin
// we no longer use this one as media3 has gottem a way better way of retrieving the playlist as seen in the next code snippet 
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

// use this method to get the playlist instead, the old one had some flaws if used incorrectly 
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun MediaItemList(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    player: () -> Player?
) {

    val player = player()

    val currentMediaItem = rememberCurrentMediaItemState(player)
    val playlistState = rememberPlaylistState(player)

    LazyColumn(contentPadding = paddingValues.plus(PaddingValues(16.dp)), verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap), modifier = modifier) {

        items(playlistState.mediaItemCount) { index ->

            val mediaItem = playlistState.getMediaItemAt(index)
            val isPlaying = currentMediaItem.mediaItem?.mediaId == mediaItem.mediaId

            MediaItemRow(
                shapes = ListItemDefaults.segmentedShapes(index = index,count = playlistState.mediaItemCount),
                isPlaying = isPlaying,
                mediaItem = mediaItem,
                onClick = {
                    playlistState.seekToMediaItem(index)
                }
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
