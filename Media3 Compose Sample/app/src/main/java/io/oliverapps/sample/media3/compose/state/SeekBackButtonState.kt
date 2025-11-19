package io.oliverapps.media3.ui.compose.state

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
fun rememberSeekBackButtonState(player: Player): SeekBackButtonState {
    val seekBackButtonState = remember(player) { SeekBackButtonState(player) }
    LaunchedEffect(player) { seekBackButtonState.observe() }
    return seekBackButtonState
}

@UnstableApi
class SeekBackButtonState(private val player: Player) {

    var isEnabled by mutableStateOf(isSeekBackEnabled(player))
        private set

    var seekBackAmountMs by mutableLongStateOf(player.seekBackIncrement)
        private set

    fun onClick() {
        player.seekBack()
    }

    suspend fun observe(): Nothing {

        isEnabled = isSeekBackEnabled(player)
        seekBackAmountMs = player.seekBackIncrement

        player.listen { events ->
            if (
                events.containsAny(
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_SEEK_BACK_INCREMENT_CHANGED,
                )
            ) {
                isEnabled = isSeekBackEnabled(this)
                seekBackAmountMs = seekBackIncrement
            }
        }
    }

    private fun isSeekBackEnabled(player: Player) =
        player.isCurrentMediaItemDynamic
}
