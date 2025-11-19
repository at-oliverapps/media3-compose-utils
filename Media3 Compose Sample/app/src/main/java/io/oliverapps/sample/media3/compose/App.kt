@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.oliverapps.sample.media3.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.oliverapps.media3.ui.compose.state.rememberMediaController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {

    val mediaController by rememberMediaController<PlaybackService>()

    mediaController?.let { player ->
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text("List Of Media") })
            },
            content = { padding ->
                MediaItemList(paddingValues = padding, player = { player })
            },
            bottomBar = {
                MiniPlayer(
                    modifier = Modifier.padding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues()),
                    player = { player }
                )
            }
        )
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LoadingIndicator()
    }

}