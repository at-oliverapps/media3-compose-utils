package io.oliverapps.media3.compose.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.ui.compose.state.rememberCurrentMediaItemState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import io.oliverapps.media3.compose.shared.ui.Constants.Shared.Icon.PlayPauseIcon
import io.oliverapps.media3.compose.shared.ui.Constants.Shared.String.PlayPauseContentDescription

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun MiniPlayer(
    player: Player?,
    modifier: Modifier = Modifier,
) {

    val playPauseButtonState = rememberPlayPauseButtonState(player)
    val mediaItemState = rememberCurrentMediaItemState(player)
    val mediaMetadataState = mediaItemState.mediaMetadata

    val isPlaying = !playPauseButtonState.showPlay

    CompactButton(
        modifier = modifier.widthIn(max = 100.dp),
        onClick = playPauseButtonState::onClick,
        shape = if (isPlaying) RoundedCornerShape(25) else RoundedCornerShape(50),
        icon = {
            Icon(imageVector = isPlaying.PlayPauseIcon, contentDescription = stringResource(isPlaying.PlayPauseContentDescription), modifier = Modifier.size(ButtonDefaults.ExtraSmallIconSize))
        },
        label = {

            val title = if (mediaMetadataState.artist == null) {
                "${mediaMetadataState.title ?: mediaMetadataState.station ?: "Unknown Title"}"
            } else {
                "${mediaMetadataState.title ?: mediaMetadataState.station ?: "Unknown Title"} - ${mediaMetadataState.artist ?: "Unknown Artist"}"
            }

            Text(title, modifier = Modifier.basicMarquee())
        }
    )
}