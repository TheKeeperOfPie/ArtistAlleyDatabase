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
import com.anilist.MediaDetailsQuery
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AppMediaPlayer
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.animethemes.AnimeThemesApi
import com.thekeeperofpie.artistalleydatabase.animethemes.AnimeThemesUtils
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeTheme
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeThemeEntry
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AnimeMediaDetailsViewModel @Inject constructor(
    private val application: Application,
    private val aniListApi: AuthedAniListApi,
    private val cdEntryDao: CdEntryDao,
    private val appJson: AppJson,
    private val animeThemesApi: AnimeThemesApi,
    val mediaPlayer: AppMediaPlayer,
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
                                .map { AnimeSongState(id = it.id, entry = it) }
                                .associateBy { it.id }
                            animeSongs.value = AnimeSongs(animeThemeEntries)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading from AnimeThemes", e)
                }
            }
        }
    }

    fun getAnimeSongState(animeSongId: String) = animeSongStates.value[animeSongId]!!

    fun onAnimeSongPlayAudioClick(animeSongId: String) {
        val state = animeSongStates.value[animeSongId]!!
        mediaPlayer.playPause(state.id, state.entry.audioUrl!!)
        animeSongStates.value.forEach { it.value.setExpanded(false) }
    }

    fun onAnimeSongProgressUpdate(animeSongId: String, progress: Float) {
        mediaPlayer.updateProgress(animeSongId, progress)
    }

    fun onAnimeSongExpandedToggle(animeSongId: String, expanded: Boolean) {
        animeSongStates.value.forEach {
            if (it.key == animeSongId) {
                if (expanded) {
                    if (!it.value.expanded()) {
                        it.value.setExpanded(true)
                        mediaPlayer.prepare(animeSongId, it.value.entry.videoUrl!!)
                    }
                } else {
                    if (it.value.expanded()) {
                        it.value.setExpanded(false)
                        mediaPlayer.pause(animeSongId)
                    }
                }
            } else {
                it.value.setExpanded(false)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        animeSongStates.value.forEach { mediaPlayer.pause(it.key) }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        animeSongStates.value.forEach { mediaPlayer.pause(it.key) }
    }

    override fun onCleared() {
        animeSongStates.value.forEach { mediaPlayer.stop(it.key) }
        super.onCleared()
    }

    fun animeSongsCollapseAll() {
        animeSongStates.value.forEach { it.value.setExpanded(false) }
        mediaPlayer.pause(null)
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
        val id: String,
        val entry: AnimeSongEntry,
    ) {

        private var _expanded by mutableStateOf(false)

        fun expanded() = _expanded

        fun setExpanded(expanded: Boolean) {
            this._expanded = expanded
        }
    }
}
