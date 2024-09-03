package com.thekeeperofpie.artistalleydatabase.media

import androidx.compose.runtime.Composable

@Composable
expect fun YouTubePlayer(state: YouTubePlayerState)

@Composable
expect fun rememberYouTubePlayerState(videoId: String): YouTubePlayerState

expect class YouTubePlayerState
