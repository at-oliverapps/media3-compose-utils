@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.oliverapps.sample.media3.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.oliverapps.media3.compose.shared.ui.PlaybackService
import io.oliverapps.media3.state.rememberMediaController
import io.oliverapps.media3.compose.shared.R as shared

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {

    val mediaController by rememberMediaController<PlaybackService>()

    Surface {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text(stringResource(shared.string.media_item_list)) })
            },
            content = { padding ->
                MediaItemList(paddingValues = padding, player = { mediaController })
            },
            bottomBar = {
                MiniPlayer(
                    modifier = Modifier.padding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues()),
                    player = { mediaController }
                )
            }
        )
    }

}