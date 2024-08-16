package com.thekeeperofpie.artistalleydatabase.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

expect class MediaPlayer {
    fun prepare(id: String, url: String)
    fun pause(id: String?)
    fun playPause(id: String, url: String)
    fun stop(id: String)
    fun updateProgress(id: String, progress: Float)
}

@Composable
expect fun MediaPlayerView(
    mediaPlayer: MediaPlayer,
    modifier: Modifier = Modifier,
    state: MediaPlayerViewState = rememberMediaPlayerViewState(),
)

@Composable
fun rememberMediaPlayerViewState() = remember { MediaPlayerViewState() }

class MediaPlayerViewState {
    var controlsVisible by mutableStateOf(false)
}
