package com.thekeeperofpie.artistalleydatabase.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual class MediaPlayer {
    // TODO: Actually add a desktop implementation
    actual fun prepare(id: String, url: String) = Unit
    actual fun pause(id: String?) = Unit
    actual fun playPause(id: String, url: String) = Unit
    actual fun stop(id: String) = Unit
    actual fun updateProgress(id: String, progress: Float) = Unit
}

@Composable
actual fun MediaPlayerView(
    mediaPlayer: MediaPlayer,
    modifier: Modifier,
    state: MediaPlayerViewState,
) = Unit
