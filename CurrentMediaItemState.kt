package io.oliverapps.radio.player.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
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

    var mediaItem by mutableStateOf(player.currentMediaItem)
        private set

    suspend fun observe(): Nothing {

        mediaItem = player.currentMediaItem

        player.listen { events ->
            if (
                events.containsAny(
                    Player.EVENT_MEDIA_ITEM_TRANSITION
                )
            ) {
               mediaItem = this.currentMediaItem
            }
        }
    }

}