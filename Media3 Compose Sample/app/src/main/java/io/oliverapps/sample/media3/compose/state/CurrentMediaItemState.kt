package io.oliverapps.media3.ui.compose.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi

@UnstableApi
@Composable
fun rememberCurrentMediaItemState(player: Player): CurrentMediaItemState {
    val currentMediaItemState = remember(player) { CurrentMediaItemState(player) }
    LaunchedEffect(player) { currentMediaItemState.observe() }
    return currentMediaItemState
}

@UnstableApi
class CurrentMediaItemState(private val player: Player) {

    var mediaId: String by mutableStateOf(MediaItem.DEFAULT_MEDIA_ID)
        private set
    var mediaMetadata: MediaMetadata by mutableStateOf(MediaMetadata.EMPTY)
        private set
    var requestMetadata: MediaItem.RequestMetadata by mutableStateOf(MediaItem.RequestMetadata.EMPTY)
        private set
    var liveConfiguration: MediaItem.LiveConfiguration by mutableStateOf(MediaItem.LiveConfiguration.UNSET)
        private set
    var clippingConfiguration: MediaItem.ClippingConfiguration by mutableStateOf(MediaItem.ClippingConfiguration.UNSET)
        private set
    var localConfiguration: MediaItem.LocalConfiguration? by mutableStateOf(null) // Officially Nullable
        private set

    suspend fun observe(): Nothing {

        fun updateState(player: Player){
            player.currentMediaItem?.mediaMetadata?.let {
                mediaMetadata = it
            }

            player.currentMediaItem?.mediaId?.let {
                mediaId = it
            }

            player.currentMediaItem?.requestMetadata?.let {
                requestMetadata = it
            }

            player.currentMediaItem?.liveConfiguration?.let {
                liveConfiguration = it
            }

            player.currentMediaItem?.clippingConfiguration?.let {
                clippingConfiguration = it
            }

            localConfiguration = player.currentMediaItem?.localConfiguration
        }

        updateState(player)

        player.listen { events ->
            if (events.containsAny(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                updateState(this)
            }
        }

    }

}

/**
 * A Composable that provides the raw MediaItem. It is simpler to use but
 * may trigger wider recompositions than the granular state holder.
 *
 * use like
 * val currentMediaItem by rememberCurrentMediaItem(player)
 */
@UnstableApi
@Composable
fun rememberCurrentMediaItem(player: Player): State<MediaItem?> {
    val state = remember(player) { CurrentMediaItem(player) }
    LaunchedEffect(player) { state.observe() }
    return state.mediaItem
}

@UnstableApi
@Composable
fun rememberCurrentMediaItemNullable(player: Player?): State<MediaItem?> {

    val currentMediaItemState = remember { mutableStateOf<MediaItem?>(null) }

    // Use DisposableEffect keyed on 'player'
    DisposableEffect(player) {

        // 1. If player is null, there is no listener to add.
        if (player == null) {
            currentMediaItemState.value = null

            // In the null case, you MUST still return a simple onDispose block.
            return@DisposableEffect onDispose { }
        }

        // --- Player is NON-NULL: Setup Listener ---
        val listener = object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentMediaItemState.value = player.currentMediaItem
            }
        }

        // Initialize state and add listener
        currentMediaItemState.value = player.currentMediaItem
        player.addListener(listener)

        // 2. Cleanup: This MUST be the last statement in the lambda.
        onDispose {
            player.removeListener(listener)
        }
    }

    return currentMediaItemState
}

@UnstableApi
private class CurrentMediaItem(private val player: Player) {

    var mediaItem = mutableStateOf(player.currentMediaItem)
        private set

    suspend fun observe(): Nothing {

        mediaItem.value = player.currentMediaItem

        player.listen { events ->
            if (events.containsAny(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                mediaItem.value = this.currentMediaItem
            }
        }
    }
}