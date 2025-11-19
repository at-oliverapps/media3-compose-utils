package io.oliverapps.sample.media3.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import io.oliverapps.media3.ui.compose.state.rememberCurrentMediaItem

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun MediaItemList(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    player: () -> Player
) {
    val currentMediaItem by rememberCurrentMediaItem(player())

    LazyColumn(contentPadding = paddingValues, modifier = modifier) {
        itemsIndexed(player().mediaItems) { index, mediaItem ->

            val isPlaying = currentMediaItem?.mediaId == mediaItem.mediaId

            ListItem(
                colors = ListItemDefaults.colors(containerColor = if (isPlaying) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent),
                modifier = Modifier.clickable {
                    player().seekToDefaultPosition(index)
                    player().play()
                },
                leadingContent = {
                    AsyncImage(
                        modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                        model = mediaItem.mediaMetadata.artworkData ?: mediaItem.mediaMetadata.artworkUri,
                        contentDescription = "List Item Artwork"
                    )
                },
                headlineContent = {
                    Text((mediaItem.mediaMetadata.title ?: mediaItem.mediaMetadata.station ?: "Unknown Title").toString())
                },
                supportingContent = mediaItem.mediaMetadata.artist?.let { { Text(it.toString()) } },
                trailingContent = if (isPlaying) { { Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null) } } else null
            )
        }

    }

}