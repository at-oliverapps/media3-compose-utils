package io.oliverapps.media3.compose.presentation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.media3.common.Player
import androidx.media3.ui.compose.state.rememberCurrentMediaItemState
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import io.oliverapps.media3.compose.shared.R as shared
import io.oliverapps.media3.compose.shared.ui.mediaItems

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun MediaItemList(
    listState: ScalingLazyListState,
    player: () -> Player?
) {

    val player = player()
    val currentMediaItem = rememberCurrentMediaItemState(player)

    ScalingLazyColumn(state = listState) {
        item {
            ListHeader {
                Text(stringResource(shared.string.media_item_list))
            }
        }

        item {
            MiniPlayer(player)
        }

        player?.mediaItems?.let {
            itemsIndexed(it) { index, mediaItem ->
                val isPlaying = currentMediaItem.mediaItem?.mediaId == mediaItem.mediaId
                MediaItemRow(mediaItem = mediaItem, isPlaying = isPlaying) {
                    player.apply {
                        seekToDefaultPosition(index)
                        prepare()
                        play()
                    }
                }

            }
        } ?: CircularProgressIndicatorItem()

    }

}
