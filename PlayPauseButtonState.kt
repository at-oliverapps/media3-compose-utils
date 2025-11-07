package io.oliverapps.radio.player.state

import android.media.session.PlaybackState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util.handlePlayPauseButtonAction
import androidx.media3.common.util.Util.shouldEnablePlayPauseButton
import androidx.media3.common.util.Util.shouldShowPlayButton

@UnstableApi
@Composable
fun rememberPlayPauseButtonState(player: Player): PlayPauseButtonState {
    val playPauseButtonState = remember(player) { PlayPauseButtonState(player) }
    LaunchedEffect(player) { playPauseButtonState.observe() }
    return playPauseButtonState
}

@UnstableApi
class PlayPauseButtonState(private val player: Player) {

    var isBuffering by mutableStateOf(player.playbackState == PlaybackState.STATE_BUFFERING)
        private set

    var isEnabled by mutableStateOf(shouldEnablePlayPauseButton(player))
        private set

    var showPlay by mutableStateOf(shouldShowPlayButton(player))
        private set

    fun onClick() {
        handlePlayPauseButtonAction(player)
    }

    suspend fun observe(): Nothing {
        isBuffering = player.playbackState == PlaybackState.STATE_BUFFERING
        showPlay = shouldShowPlayButton(player)
        isEnabled = shouldEnablePlayPauseButton(player)
        player.listen { events ->
            if (
                events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED,
                )
            ) {
                isBuffering = playbackState == PlaybackState.STATE_BUFFERING
                showPlay = shouldShowPlayButton(this)
                isEnabled = shouldEnablePlayPauseButton(this)
            }
        }
    }

}