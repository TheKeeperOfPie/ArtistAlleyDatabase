package com.thekeeperofpie.artistalleydatabase.anime.media.details

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.anilist.MediaDetailsQuery
import com.anilist.type.MediaType
import com.hoc081098.flowext.interval
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.animethemes.AnimeThemesApi
import com.thekeeperofpie.artistalleydatabase.animethemes.AnimeThemesUtils
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeTheme
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeThemeEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeMediaDetailsViewModel @Inject constructor(
    private val application: Application,
    private val aniListApi: AuthedAniListApi,
    private val cdEntryDao: CdEntryDao,
    private val appJson: AppJson,
    private val animeThemesApi: AnimeThemesApi,
) : ViewModel(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AnimeMediaDetailsViewModel"
    }

    lateinit var mediaId: String

    val loading = MutableStateFlow(false)
    val media = MutableStateFlow<MediaDetailsQuery.Data.Media?>(null)
    var errorResource = MutableStateFlow<Pair<Int, Exception?>?>(null)
    var animeSongs = MutableStateFlow<AnimeSongs?>(null)
    var cdEntries = MutableStateFlow<List<CdEntryGridModel>>(emptyList())

    private val animeSongStates = MutableStateFlow(emptyMap<String, AnimeSongState>())

    fun initialize(mediaId: String) {
        if (::mediaId.isInitialized) return
        this.mediaId = mediaId

        viewModelScope.launch(CustomDispatchers.IO) {
            loading.value = true
            try {
                media.value = aniListApi.mediaDetails(mediaId).data?.media
            } catch (e: Exception) {
                errorResource.value = R.string.anime_media_error_loading_details to e
            } finally {
                loading.value = false
            }

            cdEntries.value = cdEntryDao.searchSeriesByMediaId(appJson, mediaId)
                .map { CdEntryGridModel.buildFromEntry(application, it) }

            if (media.value?.type == MediaType.ANIME) {
                try {
                    val anime = animeThemesApi.getAnime(mediaId)
                    val animeThemeEntries = anime
                        ?.animethemes
                        ?.mapNotNull {
                            val video = it.animeThemeEntries
                                .firstOrNull()
                                ?.videos
                                ?.firstOrNull()
                            AnimeSongEntry(
                                id = it.id,
                                type = it.type,
                                song = it.song ?: return@mapNotNull null,
                                animeThemeEntries = it.animeThemeEntries,
                                videoUrl = video?.link,
                                audioUrl = video?.audio?.link,
                                link = AnimeThemesUtils.buildWebsiteLink(anime, it),
                            )
                        }
                        .orEmpty()
                    if (animeThemeEntries.isNotEmpty()) {
                        withContext(CustomDispatchers.Main) {
                            animeSongStates.value = animeThemeEntries
                                .map {
                                    AnimeSongState(
                                        application = application,
                                        id = it.id,
                                        entry = it,
                                    )
                                }
                                .associateBy { it.id }
                            animeSongs.value = AnimeSongs(animeThemeEntries)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading from AnimeThemes", e)
                }
            }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            animeSongStates
                .flatMapLatest {
                    combine(it.values.map { state -> state.playing.map { state to it } }) { it }
                        .flatMapLatest { array ->
                            if (array.any { it.second }) {
                                interval(Duration.ZERO, 1.seconds)
                                    .map { array }
                            } else {
                                emptyFlow()
                            }
                        }
                }
                .collect {
                    it.forEach { it.first.updateProgress() }
                }
        }
    }

    fun getAnimeSongState(animeThemeId: String) = animeSongStates.value[animeThemeId]!!

    fun onAnimeThemePlayClick(animeThemeId: String) {
        val state = animeSongStates.value[animeThemeId]!!
        if (state.playing.value) {
            state.pause()
        } else {
            state.play()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        animeSongStates.value.forEach { it.value.pause() }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        animeSongStates.value.forEach { it.value.pause() }
    }

    override fun onCleared() {
        animeSongStates.value.forEach { it.value.release() }
        super.onCleared()
    }

    data class AnimeSongs(
        val entries: List<AnimeSongEntry>,
    )

    data class AnimeSongEntry(
        val id: String,
        val type: AnimeTheme.Type?,
        val song: AnimeTheme.Song,
        val animeThemeEntries: List<AnimeThemeEntry>,
        val episodes: String? = animeThemeEntries.firstOrNull()?.episodes,
        val videoUrl: String?,
        val audioUrl: String?,
        val link: String?,
    )

    // State separated from immutable data so that recomposition is as granular as possible
    class AnimeSongState(
        application: Application,
        val id: String,
        val entry: AnimeSongEntry,
    ) {
        var playing = MutableStateFlow(false)
        var progress by mutableStateOf(0f)

        val player: ExoPlayer?

        private var _expanded by mutableStateOf(false)
        private var preparedForVideo: Boolean? = null

        init {
            if (entry.videoUrl == null && entry.audioUrl == null) {
                player = null
            } else {
                player = ExoPlayer.Builder(application).build()
                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        when (playbackState) {
                            Player.STATE_IDLE,
                            Player.STATE_BUFFERING,
                            Player.STATE_READY -> Unit
                            Player.STATE_ENDED -> playing.value = false
                        }
                    }
                })
            }
        }

        fun expanded() = _expanded

        fun setExpanded(expanded: Boolean) {
            this._expanded = expanded
            if (expanded) {
                player!!.setMediaItem(MediaItem.fromUri(entry.videoUrl!!))
                player.prepare()
            } else {
                pause()
            }
        }

        fun play() {
            player ?: return
            playing.value = true
            if (preparedForVideo == null) {
                preparedForVideo = if (_expanded) {
                    player.setMediaItem(MediaItem.fromUri((entry.videoUrl ?: entry.audioUrl)!!))
                    true
                } else {
                    player.setMediaItem(MediaItem.fromUri(entry.audioUrl!!))
                    false
                }

                player.prepare()
            } else if (_expanded && preparedForVideo == false) {
                player.setMediaItem(MediaItem.fromUri((entry.videoUrl ?: entry.audioUrl)!!))
                preparedForVideo = true
                player.prepare()
            }

            player.play()
        }

        fun pause() {
            player ?: return
            playing.value = false
            player.pause()
        }

        fun release() = player?.release()

        fun updateProgress() {
            if (!playing.value) return
            player?.let { progress = it.currentPosition / it.duration.toFloat() }
        }

        fun updateProgress(progress: Float) {
            this.progress = progress
            player?.seekTo((player.duration * progress).toLong())
        }
    }
}
