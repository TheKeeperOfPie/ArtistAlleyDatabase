package com.thekeeperofpie.artistalleydatabase.anime

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.hoc081098.flowext.interval
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AppMediaPlayer(
    application: ScopedApplication,
    okHttpClient: OkHttpClient,
) {
    // TODO: ID doesn't consider nested duplicate screens; unsure if this matters
    var playingState = MutableStateFlow<Pair<String?, Boolean>>(null to false)
    var progress by mutableFloatStateOf(0f)

    val player = ExoPlayer.Builder(application.app)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(
                DataSourceFactory(application.app, okHttpClient)
            )
        )
        .build()
        .apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        Player.STATE_IDLE,
                        Player.STATE_BUFFERING,
                        Player.STATE_READY,
                        -> Unit
                        Player.STATE_ENDED -> {
                            playingState.value = playingState.value.copy(second = false)
                            seekTo(C.TIME_UNSET)
                            stop()
                        }
                    }
                }
            })
        }

    private class DataSourceFactory(
        application: Application,
        okHttpClient: OkHttpClient,
    ) : DataSource.Factory {
        private val actualFactory = OkHttpDataSource.Factory(okHttpClient)

        private val cache = SimpleCache(
            File(application.cacheDir, "exoplayer"),
            LeastRecentlyUsedCacheEvictor(500L * 1024L * 1024L),
            StandaloneDatabaseProvider(application)
        )

        override fun createDataSource() = CacheDataSource(
            cache,
            actualFactory.createDataSource(),
            FileDataSource(),
            CacheDataSink(cache, C.LENGTH_UNSET.toLong()),
            CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
            null
        )
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
