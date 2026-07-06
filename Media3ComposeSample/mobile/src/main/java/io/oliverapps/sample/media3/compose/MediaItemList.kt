@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.oliverapps.sample.media3.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.ui.compose.state.rememberCurrentMediaItemState
import androidx.media3.ui.compose.state.rememberPlaylistState

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun MediaItemList(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    player: () -> Player?
) {

    val player = player()

    val currentMediaItem = rememberCurrentMediaItemState(player)
    val playlistState = rememberPlaylistState(player)

    LazyColumn(contentPadding = paddingValues.plus(PaddingValues(16.dp)), verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap), modifier = modifier) {

        items(playlistState.mediaItemCount) { index ->

            val mediaItem = playlistState.getMediaItemAt(index)
            val isPlaying = currentMediaItem.mediaItem?.mediaId == mediaItem.mediaId

            MediaItemRow(
                shapes = ListItemDefaults.segmentedShapes(index = index,count = playlistState.mediaItemCount),
                isPlaying = isPlaying,
                mediaItem = mediaItem,
                onClick = {
                    playlistState.seekToMediaItem(index)
                }
            )

        }

    }

}

