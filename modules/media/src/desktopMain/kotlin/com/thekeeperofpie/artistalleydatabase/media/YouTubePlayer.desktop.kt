package com.thekeeperofpie.artistalleydatabase.media

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun YouTubePlayer(state: YouTubePlayerState) {
    // TODO
    Text(text = "YouTube not supported")
}

@Composable
actual fun rememberYouTubePlayerState(videoId: String) = remember { YouTubePlayerState() }

actual class YouTubePlayerState
