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
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
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

            val mediaValue = media.value
            if (mediaValue?.type == MediaType.ANIME) {
                try {
                    val anime = animeThemesApi.getAnime(mediaId)
                    val songEntries = anime
                        ?.animethemes
                        ?.map {
                            val video = it.animeThemeEntries
                                .firstOrNull()
                                ?.videos
                                ?.firstOrNull()
                            AnimeSongEntry(
                                id = it.id,
                                type = it.type,
                                title = it.song?.title.orEmpty(),
                                spoiler = it.animeThemeEntries.any { it.spoiler },
                                artists = it.song?.artists?.map { buildArtist(mediaValue, it) }
                                    .orEmpty(),
                                animeThemeEntries = it.animeThemeEntries,
                                videoUrl = video?.link,
                                audioUrl = video?.audio?.link,
                                link = AnimeThemesUtils.buildWebsiteLink(anime, it),
                            )
                        }
                        .orEmpty()

                    if (songEntries.isNotEmpty()) {
                        val songStates = songEntries
                            .map { AnimeSongState(id = it.id, entry = it) }
                            .associateBy { it.id }

                        withContext(CustomDispatchers.Main) {
                            animeSongStates.value = songStates
                            animeSongs.value = AnimeSongs(songEntries)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading from AnimeThemes", e)
                }
            }
        }
    }

    // TODO: Something better than exact string matching
    private fun buildArtist(
        media: MediaDetailsQuery.Data.Media,
        artist: AnimeTheme.Song.Artist,
    ): AnimeSongEntry.Artist {
        val nodes = media.characters?.nodes?.filterNotNull()
        val node = nodes?.find {
            val characterName = it.name ?: return@find false
            characterName.full == artist.character ||
                    (characterName.alternative?.any { it == artist.character } == true)
        }

        val edgeAndVoiceActor = media.characters?.edges
            ?.asSequence()
            ?.filterNotNull()
            ?.mapNotNull { edge ->
                edge.voiceActors?.filterNotNull()?.firstOrNull {
                    it.name?.full == artist.name ||
                            (it.name?.alternative?.any { it == artist.name } == true)
                }?.let { edge to it }
            }
            ?.firstOrNull()

        val character = (node
            ?: edgeAndVoiceActor?.let { (edge, _) -> nodes?.find { it.id == edge.node?.id } })
            ?.let {
                AnimeSongEntry.Artist.Character(
                    aniListId = it.id.toString(),
                    image = it.image?.large,
                    name = it.name?.userPreferred ?: artist.character ?: "",
                )
            }

        val artistImage = edgeAndVoiceActor?.second?.image?.large
            ?: (artist.images.firstOrNull {
                it.facet == AnimeTheme.Song.Artist.Images.Facet.SmallCover
            } ?: artist.images.firstOrNull())?.link

        return AnimeSongEntry.Artist(
            id = artist.id,
            aniListId = edgeAndVoiceActor?.second?.id?.toString(),
            animeThemesSlug = artist.slug,
            name = artist.name,
            image = artistImage,
            asCharacter = !artist.character.isNullOrBlank(),
            character = character,
        )
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
        val title: String,
        val spoiler: Boolean,
        val artists: List<Artist>,
        val animeThemeEntries: List<AnimeThemeEntry>,
        val episodes: String? = animeThemeEntries.firstOrNull()?.episodes,
        val videoUrl: String?,
        val audioUrl: String?,
        val link: String?,
    ) {
        data class Artist(
            val id: String,
            val aniListId: String?,
            val animeThemesSlug: String,
            val name: String,
            val image: String?,
            val asCharacter: Boolean,
            val character: Character?,
        ) {
            val link by lazy {
                if (aniListId != null) {
                    AniListUtils.staffUrl(aniListId)
                } else {
                    AnimeThemesUtils.artistUrl(animeThemesSlug)
                }
            }

            data class Character(
                val aniListId: String,
                val image: String?,
                val name: String,
            )
        }
    }

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
