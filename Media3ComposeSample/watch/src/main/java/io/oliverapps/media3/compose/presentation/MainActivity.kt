package io.oliverapps.media3.compose.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.oliverapps.media3.compose.presentation.theme.Media3ComposeSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Media3ComposeSampleTheme {
                App()
            }
        }
    }
}
