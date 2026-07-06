@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.oliverapps.sample.media3.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import coil.compose.AsyncImage
import io.oliverapps.media3.compose.shared.ui.Constants

@Composable
fun MediaItemRow(
    modifier: Modifier = Modifier,
    mediaItem: MediaItem,
    isPlaying: Boolean,
    shapes: ListItemShapes,
    onClick: () -> Unit
) = SegmentedListItem(
    colors = ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shapes = shapes,
    modifier = modifier,
    checked = isPlaying,
    onCheckedChange = { onClick() },
    leadingContent = {
        AsyncImage(
            modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant),
            model = mediaItem.mediaMetadata.artworkData ?: mediaItem.mediaMetadata.artworkUri,
            contentDescription = "List Item Artwork"
        )
    },
    content = {
        Text((mediaItem.mediaMetadata.title ?: mediaItem.mediaMetadata.station ?: "Unknown Title").toString())
    },
    supportingContent = mediaItem.mediaMetadata.artist?.let { { Text(it.toString()) } },
    trailingContent = if (isPlaying) { { Icon(imageVector = Constants.Shared.Icon.Equalizer, contentDescription = null) } } else null
)