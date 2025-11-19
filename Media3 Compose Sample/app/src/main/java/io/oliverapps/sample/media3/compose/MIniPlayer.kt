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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.rememberNextButtonState
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPreviousButtonState
import coil.compose.AsyncImage
import io.oliverapps.media3.ui.compose.state.rememberMediaMetadata
import io.oliverapps.media3.ui.compose.state.rememberSeekBackButtonState
import io.oliverapps.media3.ui.compose.state.rememberSeekForwardButtonState


@OptIn(UnstableApi::class)
@Composable
fun MiniPlayer(
    modifier: Modifier= Modifier,
    player: () -> Player
) {

    val player = player()

    val playPauseButtonState = rememberPlayPauseButtonState(player)

    val previousButtonState = rememberPreviousButtonState(player)
    val nextButtonState = rememberNextButtonState(player)

    val seekBackButtonState = rememberSeekBackButtonState(player)
    val seekForwardButtonState = rememberSeekForwardButtonState(player)

    val mediaMetadataState = rememberMediaMetadata(player)

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
                    .size(40.dp)
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
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            content = {

                onRewind?.let {
                    IconButton(
                        onClick = it,
                        content = {
                            Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Fast Rewind")
                        }
                    )
                } ?: Box(modifier = Modifier.minimumInteractiveComponentSize())

                onPrevious?.let {
                    IconButton(
                        onClick = it,
                        content = {
                            Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous")
                        }
                    )
                } ?: Box(modifier = Modifier.minimumInteractiveComponentSize())

                onTogglePlayback?.let {
                    FilledIconButton(
                        shape = if (isPlaying) IconButtonDefaults.smallSquareShape else IconButtonDefaults.smallRoundShape,
                        onClick = it,
                        content = {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                        }
                    )
                } ?: Box(modifier = Modifier.minimumInteractiveComponentSize())

                onNext?.let {
                    IconButton(
                        onClick = it,
                        content = {
                            Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next")
                        }
                    )
                } ?: Box(modifier = Modifier.minimumInteractiveComponentSize())

                onForward?.let {
                    IconButton(
                        onClick = it,
                        content = { 
                            Icon(imageVector = Icons.Default.FastForward, contentDescription = "Fast Forward")
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