package com.thekeeperofpie.artistalleydatabase.media

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@OptIn(ExperimentalCoroutinesApi::class)
actual class MediaPlayer(
    scope: CoroutineScope,
    application: Application,
    okHttpClient: OkHttpClient,
    enableCache: Boolean,
) {
    // TODO: ID doesn't consider nested duplicate screens; unsure if this matters
    actual var progress by mutableFloatStateOf(0f)
    actual var activeId by mutableStateOf<String?>(null)
    actual var playing by mutableStateOf(false)

    val player = ExoPlayer.Builder(application)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(
                DataSourceFactory(application, okHttpClient, enableCache)
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
                            playing = false
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
        enableCache: Boolean,
    ) : DataSource.Factory {
        private val actualFactory = OkHttpDataSource.Factory(okHttpClient)

        private val cache = if (enableCache) {
            SimpleCache(
                File(application.cacheDir, "exoplayer"),
                LeastRecentlyUsedCacheEvictor(500L * 1024L * 1024L),
                StandaloneDatabaseProvider(application)
            )
        } else null

        override fun createDataSource() = cache?.let {
            CacheDataSource(
                cache,
                actualFactory.createDataSource(),
                FileDataSource(),
                CacheDataSink(cache, C.LENGTH_UNSET.toLong()),
                CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                null
            )
        } ?: actualFactory.createDataSource()
    }

    init {
        scope.launch(Dispatchers.Main) {
            snapshotFlow { activeId to playing }
                .flatMapLatest {
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

    actual fun prepare(id: String, url: String) {
        val previousId = activeId
        activeId = id
        playing = false
        if (previousId != id) {
            player.pause()
        }

        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
    }

    actual fun pause(id: String?) {
        if (id != null && activeId != id) return
        playing = false
        player.pause()
    }

    actual fun playPause(id: String, url: String) {
        if (activeId == id) {
            val wasPlaying = playing
            playing = !wasPlaying
            if (wasPlaying) {
                player.pause()
            } else {
                player.play()
            }
        } else {
            activeId = id
            playing = true
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }

    actual fun stop(id: String) {
        if (activeId != id) return
        playing = false
        player.stop()
    }

    actual fun updateProgress(id: String, progress: Float) {
        if (activeId != id) return
        this.progress = progress
        player.seekTo((player.duration * progress).toLong())
    }
}
