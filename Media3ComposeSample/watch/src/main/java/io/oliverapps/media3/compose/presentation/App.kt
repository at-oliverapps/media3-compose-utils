package io.oliverapps.media3.compose.presentation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.TimeText
import io.oliverapps.media3.compose.shared.ui.PlaybackService
import io.oliverapps.media3.state.rememberMediaController

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun App() {

    val mediaController by rememberMediaController<PlaybackService>()
    val listState = rememberScalingLazyListState()

    AppScaffold {
        ScreenScaffold(
            scrollState = listState,
            timeText = {
                TimeText()
            },
            content = {
                MediaItemList(listState = listState, player = { mediaController })
            }
        )
    }

}