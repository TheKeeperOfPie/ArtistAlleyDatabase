package com.thekeeperofpie.artistalleydatabase.media

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.util.concurrent.atomic.AtomicReference

@Composable
actual fun YouTubePlayer(state: YouTubePlayerState) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val player = remember { AtomicReference<YouTubePlayer>(null) }
    AndroidView(
        factory = {
            YouTubePlayerView(it).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                    override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                        player.set(youTubePlayer)
                        youTubePlayer.cueVideo(state.videoId, state.playbackPosition)
                        youTubePlayer.addListener(object :
                            AbstractYouTubePlayerListener() {
                            override fun onCurrentSecond(
                                youTubePlayer: YouTubePlayer,
                                second: Float,
                            ) {
                                state.playbackPosition = second
                            }
                        })
                    }
                })
            }
        },
        onRelease = {
            lifecycleOwner.lifecycle.removeObserver(it)
            it.release()
        },
        update = {
            lifecycleOwner.lifecycle.addObserver(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
    )
}

@Composable
actual fun rememberYouTubePlayerState(videoId: String) = remember { YouTubePlayerState(videoId) }

actual data class YouTubePlayerState(val videoId: String) {
    // This should not be state because updating it should not recompose
    var playbackPosition = 0f
}
