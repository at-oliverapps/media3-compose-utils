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
fun rememberSeekBackButtonState(player: Player?): SeekBackButtonState {
    val seekBackButtonState = remember(player) { SeekBackButtonState(player) }
    LaunchedEffect(player) { seekBackButtonState.observe() }
    return seekBackButtonState
}

@UnstableApi
class SeekBackButtonState(private val player: Player?) {

    var isEnabled by mutableStateOf(false)
        private set

    var seekBackAmountMs by mutableLongStateOf(0)
        private set

    fun onClick() {
        player?.seekBack()
    }

    private val playerStateObserver: PlayerStateObserver? = player?.observeState( Player.EVENT_TIMELINE_CHANGED, Player.EVENT_SEEK_BACK_INCREMENT_CHANGED) {
        isEnabled = isSeekBackEnabled(it)
        seekBackAmountMs = it.seekBackIncrement
    }

    suspend fun observe() {
        playerStateObserver?.observe()
    }

    private fun isSeekBackEnabled(player: Player) = player.isCurrentMediaItemDynamic

}
