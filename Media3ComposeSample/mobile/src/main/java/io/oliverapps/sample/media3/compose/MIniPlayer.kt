package io.oliverapps.sample.media3.compose

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberCurrentMediaItemState
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import androidx.media3.ui.compose.state.rememberSeekBackButtonState
import androidx.media3.ui.compose.state.rememberSeekForwardButtonState
import coil.compose.AsyncImage
import io.oliverapps.media3.compose.shared.ui.Constants
import io.oliverapps.media3.compose.shared.ui.Constants.Shared.Icon.PlayPauseIcon
import io.oliverapps.media3.compose.shared.ui.Constants.Shared.String.PlayPauseContentDescription

@OptIn(UnstableApi::class)
@Composable
fun MiniPlayer(
    modifier: Modifier= Modifier,
    player: () -> Player?
) {

    val player = player()

    val playPauseButtonState = rememberPlayPauseButtonState(player)
    val previousButtonState = rememberPreviousButtonState(player)
    val nextButtonState = rememberNextButtonState(player)
    val seekBackButtonState = rememberSeekBackButtonState(player)
    val seekForwardButtonState = rememberSeekForwardButtonState(player)
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

@Preview
@Composable
private fun MiniPlayerPreview() {
    var isPlaying by rememberSaveable { mutableStateOf(true) }
    MiniPlayer(
        isPlaying = isPlaying,
        artwork = null,
        title = "Unknown Title",
        artist = "Unknown Artist",
        album = "Unknown Album",
        onRewind = {},
        onPrevious = {},
        onTogglePlayback = { isPlaying = !isPlaying },
        onNext = {},
        onForward = {},
    )

}