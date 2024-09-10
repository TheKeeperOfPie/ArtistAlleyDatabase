package com.thekeeperofpie.artistalleydatabase.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
actual class MediaPlayer {
    // TODO
    actual var progress = 0f
    actual var activeId: String? = null
    actual var playing = false
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
