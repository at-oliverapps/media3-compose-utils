package io.oliverapps.media3.compose.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.dynamicColorScheme

@Composable
fun Media3ComposeSampleTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    MaterialTheme(
        colorScheme = dynamicColorScheme(context) ?: MaterialTheme.colorScheme,
        content = content
    )
}