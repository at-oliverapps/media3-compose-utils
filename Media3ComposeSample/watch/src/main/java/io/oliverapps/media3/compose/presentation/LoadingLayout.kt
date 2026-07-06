package io.oliverapps.media3.compose.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.Companion
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListScope
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.CircularProgressIndicatorDefaults
import androidx.wear.compose.material3.PlaceholderState
import androidx.wear.compose.material3.ProgressIndicatorColors
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.placeholder
import androidx.wear.compose.material3.placeholderShimmer
import androidx.wear.compose.material3.rememberPlaceholderState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices

@Composable
fun LoadingLayout(
    modifier: Modifier = Modifier
) = Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    CircularProgressIndicator()
}

fun ScalingLazyListScope.CircularProgressIndicatorItem(
    modifier: Modifier = Modifier,
    strokeWidth: Dp = CircularProgressIndicatorDefaults.IndeterminateStrokeWidth,
    gapSize: Dp = CircularProgressIndicatorDefaults.calculateRecommendedGapSize(strokeWidth),
) = item {
    CircularProgressIndicator(
        modifier = modifier,
        strokeWidth = strokeWidth,
        gapSize = gapSize
    )
}