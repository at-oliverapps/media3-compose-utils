package io.oliverapps.radio.player.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi

@UnstableApi
@Composable
fun rememberSeekForwardButtonState(player: Player): SeekForwardButtonState {
    val seekForwardButtonState = remember(player) { SeekForwardButtonState(player) }
    LaunchedEffect(player) { seekForwardButtonState.observe() }
    return seekForwardButtonState
}

@UnstableApi
class SeekForwardButtonState(private val player: Player) {

    var isEnabled by mutableStateOf(isSeekForwardEnabled(player))
        private set

    var seekForwardAmountMs by mutableLongStateOf(player.seekForwardIncrement)
        private set

    fun onClick() {
        player.seekForward()
    }

    suspend fun observe(): Nothing {

        isEnabled = isSeekForwardEnabled(player)
        seekForwardAmountMs = player.seekForwardIncrement

        player.listen { events ->
            if (
                events.containsAny(
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_SEEK_FORWARD_INCREMENT_CHANGED,
                )
            ) {
                isEnabled = isSeekForwardEnabled(this)
                seekForwardAmountMs = seekForwardIncrement
            }
        }
    }

    private fun isSeekForwardEnabled(player: Player) =
        player.isCurrentMediaItemDynamic

}