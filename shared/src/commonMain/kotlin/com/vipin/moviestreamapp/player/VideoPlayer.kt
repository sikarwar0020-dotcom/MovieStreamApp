package com.vipin.moviestreamapp.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    videoUrl: String,
    adVideoUrl: String,
    adPositionSeconds: Long,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true
)