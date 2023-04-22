package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.hoc081098.flowext.interval
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class AppMediaPlayer(
    application: ScopedApplication
) {
    // TODO: ID doesn't consider nested duplicate screens; unsure if this matters
    var playingState = MutableStateFlow<Pair<String?, Boolean>>(null to false)
    var progress by mutableStateOf(0f)

    val player = ExoPlayer.Builder(application.app).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_IDLE,
                    Player.STATE_BUFFERING,
                    Player.STATE_READY -> Unit
                    Player.STATE_ENDED -> {
                        playingState.value = playingState.value.copy(second = false)
                    }
                }
            }
        })
    }

    init {
        application.scope.launch(CustomDispatchers.Main) {
            playingState.flatMapLatest {
                if (it.first != null && it.second) {
                    interval(Duration.ZERO, 1.seconds)
                } else {
                    emptyFlow()
                }
            }
                .collect {
                    progress = player.currentPosition / player.duration.toFloat()
                }
        }
    }

    fun prepare(id: String, url: String) {
        val currentId = playingState.value.first
        playingState.value = id to false
        if (currentId != id) {
            player.pause()
        }

        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
    }

    fun pause(id: String?) {
        val state = playingState.value
        if (id == null) {
            playingState.value = state.copy(second = false)
            player.pause()
        } else {
            if (state.first != id) return
            playingState.value = id to false
            player.pause()
        }
    }

    fun playPause(id: String, url: String) {
        val state = playingState.value
        if (state.first == id) {
            playingState.value = id to !state.second
            if (state.second) {
                player.pause()
            } else {
                player.play()
            }
        } else {
            playingState.value = id to true
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }

    fun stop(id: String) {
        val state = playingState.value
        if (state.first != id) return
        playingState.value = state.copy(second = false)
        player.stop()
    }

    fun updateProgress(id: String, progress: Float) {
        val state = playingState.value
        if (state.first != id) return
        this.progress = progress
        player.seekTo((player.duration * progress).toLong())
    }
}
