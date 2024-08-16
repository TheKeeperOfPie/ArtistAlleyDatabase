package com.thekeeperofpie.artistalleydatabase.media

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@Composable
actual fun MediaPlayerView(
    mediaPlayer: MediaPlayer,
    modifier: Modifier,
    state: MediaPlayerViewState,
) {
    AndroidView(
        factory = {
            @Suppress("UnsafeOptInUsageError")
            PlayerView(it).apply {
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE)
                setControllerVisibilityListener(
                    PlayerView.ControllerVisibilityListener {
                        state.controlsVisible = it == View.VISIBLE
                    }
                )
            }
        },
        update = { it.player = mediaPlayer.player },
        onReset = { it.player = null },
        onRelease = { it.player = null },
        modifier = modifier,
    )
}
