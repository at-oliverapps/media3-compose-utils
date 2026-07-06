package io.oliverapps.media3.compose.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.media3.common.MediaItem
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextToggleButtonDefaults
import coil.compose.AsyncImage
import io.oliverapps.media3.compose.shared.ui.Constants

@Composable
fun MediaItemRow(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem,
    isPlaying: Boolean,
    onClick: () -> Unit
) = Button(
    shape = if (isPlaying) TextToggleButtonDefaults.checkedShape else TextToggleButtonDefaults.shape,
    colors = if (isPlaying) ButtonDefaults.filledVariantButtonColors() else ButtonDefaults.filledTonalButtonColors(),
    modifier = modifier.fillMaxWidth(),
    onClick = onClick,
    icon = {
        Box(modifier = Modifier.size(ButtonDefaults.ExtraLargeIconSize), propagateMinConstraints = true) {
            when (isPlaying) {
                true -> Icon(imageVector = Constants.Shared.Icon.Equalizer, contentDescription = null)
                false -> AsyncImage(modifier = Modifier.clip(CircleShape), model = mediaItem.mediaMetadata.artworkData ?: mediaItem.mediaMetadata.artworkUri, contentDescription = null)
            }
        }
    },
    label = { Text((mediaItem.mediaMetadata.title ?: mediaItem.mediaMetadata.station ?: "Unknown Title").toString()) },
    secondaryLabel = mediaItem.mediaMetadata.artist?.let { { Text(it.toString()) } }
)
