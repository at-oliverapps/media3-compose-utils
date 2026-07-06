package io.oliverapps.media3.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.PlayerStateObserver
import androidx.media3.ui.compose.state.observeState

@UnstableApi
@Composable
fun rememberSeekForwardButtonState(player: Player?): SeekForwardButtonState {
    val seekForwardButtonState = remember(player) { SeekForwardButtonState(player) }
    LaunchedEffect(player) { seekForwardButtonState.observe() }
    return seekForwardButtonState
}

@UnstableApi
class SeekForwardButtonState(private val player: Player?) {

    var isEnabled by mutableStateOf(false)
        private set

    var seekForwardAmountMs by mutableLongStateOf(0)
        private set

    fun onClick() {
        player?.seekForward()
    }

    private val playerStateObserver: PlayerStateObserver? = player?.observeState( Player.EVENT_TIMELINE_CHANGED, Player.EVENT_SEEK_BACK_INCREMENT_CHANGED) {
        isEnabled = isSeekForwardEnabled(it)
        seekForwardAmountMs = it.seekForwardIncrement
    }

    suspend fun observe() {
        playerStateObserver?.observe()
    }

    private fun isSeekForwardEnabled(player: Player) = player.isCurrentMediaItemDynamic

}
