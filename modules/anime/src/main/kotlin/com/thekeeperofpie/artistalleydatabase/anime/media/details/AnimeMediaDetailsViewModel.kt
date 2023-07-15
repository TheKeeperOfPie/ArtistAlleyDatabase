package com.thekeeperofpie.artistalleydatabase.anime.media.details

import android.app.Application
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.MediaDetailsQuery
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AppMediaPlayer
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyStatusAndIgnored
import com.thekeeperofpie.artistalleydatabase.animethemes.AnimeThemesApi
import com.thekeeperofpie.artistalleydatabase.animethemes.AnimeThemesUtils
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeTheme
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@HiltViewModel
class AnimeMediaDetailsViewModel @Inject constructor(
    private val application: Application,
    private val aniListApi: AuthedAniListApi,
    private val cdEntryDao: CdEntryDao,
    private val appJson: AppJson,
    private val animeThemesApi: AnimeThemesApi,
    val mediaPlayer: AppMediaPlayer,
    oAuthStore: AniListOAuthStore,
    val statusController: MediaListStatusController,
    val ignoreList: AnimeMediaIgnoreList,
) : ViewModel(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AnimeMediaDetailsViewModel"
    }

    val viewer = aniListApi.authedUser
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    lateinit var mediaId: String

    val hasAuth = oAuthStore.hasAuth

    var loading by mutableStateOf(true)
    var entry by mutableStateOf<AnimeMediaDetailsScreen.Entry?>(null)
    var listStatus by mutableStateOf<MediaListStatusController.Update?>(null)

    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)
    var animeSongs by mutableStateOf<AnimeSongs?>(null)
    var cdEntries by mutableStateOf(emptyList<CdEntryGridModel>())

    var trailerPlaybackPosition = 0f

    private var animeSongStates by mutableStateOf(emptyMap<String, AnimeSongState>())

    fun initialize(mediaId: String) {
        if (::mediaId.isInitialized) return
        this.mediaId = mediaId

        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                val media = aniListApi.mediaDetails(mediaId)
                val relations = media.relations?.edges?.filterNotNull()
                    ?.mapNotNull {
                        val node = it.node ?: return@mapNotNull null
                        val relation = it.relationType ?: return@mapNotNull null
                        AnimeMediaDetailsScreen.Entry.Relation(
                            it.id.toString(),
                            relation,
                            AnimeMediaListRow.Entry(node)
                        )
                    }
                    .orEmpty()
                    .sortedBy { AnimeMediaDetailsScreen.RELATION_SORT_ORDER.indexOf(it.relation) }

                val recommendations = media.recommendations?.edges?.filterNotNull()
                    ?.mapNotNull {
                        val node = it.node ?: return@mapNotNull null
                        val media = node.mediaRecommendation ?: return@mapNotNull null
                        AnimeMediaDetailsScreen.Entry.Recommendation(
                            node.id.toString(),
                            node.rating,
                            AnimeMediaListRow.Entry(media)
                        )
                    }
                    .orEmpty()

                val mediaIds = setOf(media.id.toString()) +
                        relations.map { it.entry.media.id.toString() } +
                        recommendations.map { it.entry.media.id.toString() }
                combine(
                    statusController.allChanges(mediaIds),
                    ignoreList.updates,
                    ::Pair,
                )
                    .mapLatest { (statuses, ignoredIds) ->
                        AnimeMediaDetailsScreen.Entry(
                            mediaId,
                            media,
                            relations = relations.map {
                                applyStatusAndIgnored(
                                    statuses,
                                    ignoredIds,
                                    it,
                                    { it.entry },
                                    it.entry.media,
                                    copy = { mediaListStatus, ignored ->
                                        copy(
                                            entry = AnimeMediaListRow.Entry(
                                                media = it.entry.media,
                                                mediaListStatus = mediaListStatus,
                                                ignored = ignored,
                                            )
                                        )
                                    }
                                )
                            },
                            recommendations = recommendations.map {
                                applyStatusAndIgnored(
                                    statuses,
                                    ignoredIds,
                                    it,
                                    { it.entry },
                                    it.entry.media,
                                    copy = { mediaListStatus, ignored ->
                                        copy(
                                            entry = AnimeMediaListRow.Entry(
                                                media = it.entry.media,
                                                mediaListStatus = mediaListStatus,
                                                ignored = ignored,
                                            )
                                        )
                                    }
                                )
                            }
                        )
                    }
                    .collectLatest {
                        withContext(CustomDispatchers.Main) {
                            entry = it
                        }
                    }
            } catch (e: Exception) {
                withContext(CustomDispatchers.Main) {
                    errorResource = R.string.anime_media_error_loading_details to e
                }
            } finally {
                withContext(CustomDispatchers.Main) {
                    loading = false
                }
            }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry }
                .flowOn(CustomDispatchers.Main)
                .filterNotNull()
                .flatMapLatest {
                    combine(flowOf(it), statusController.allChanges(it.mediaId), ::Pair)
                }
                .mapLatest { (entry, update) ->
                    val mediaListEntry = entry.media.mediaListEntry
                    MediaListStatusController.Update(
                        mediaId = entry.mediaId,
                        entry = if (update == null) mediaListEntry else update.entry,
                    )
                }
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        listStatus = it
                    }
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            val cdEntries = cdEntryDao.searchSeriesByMediaId(appJson, mediaId)
                .map { CdEntryGridModel.buildFromEntry(application, it) }
            withContext(CustomDispatchers.Main) {
                this@AnimeMediaDetailsViewModel.cdEntries = cdEntries
            }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry }
                .flowOn(CustomDispatchers.Main)
                .collectLatest {
                    val media = it?.media
                    if (media?.type == MediaType.ANIME) {
                        try {
                            val anime = animeThemesApi.getAnime(mediaId)
                            val songEntries = anime
                                ?.animethemes
                                ?.flatMap { animeTheme ->
                                    animeTheme.animeThemeEntries.map {
                                        val video = it.videos.firstOrNull()
                                        AnimeSongEntry(
                                            id = it.id,
                                            type = animeTheme.type,
                                            title = animeTheme.song?.title.orEmpty(),
                                            spoiler = it.spoiler,
                                            artists = animeTheme.song?.artists
                                                ?.map { buildArtist(media, it) }
                                                .orEmpty(),
                                            episodes = it.episodes,
                                            videoUrl = video?.link,
                                            audioUrl = video?.audio?.link,
                                            link = AnimeThemesUtils.buildWebsiteLink(
                                                anime,
                                                animeTheme,
                                                it
                                            ),
                                        )
                                    }
                                }
                                .orEmpty()

                            if (songEntries.isNotEmpty()) {
                                val songStates = songEntries
                                    .map { AnimeSongState(id = it.id, entry = it) }
                                    .associateBy { it.id }

                                withContext(CustomDispatchers.Main) {
                                    animeSongStates = songStates
                                    animeSongs = AnimeSongs(songEntries)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error loading from AnimeThemes", e)
                        }
                    }
                }
        }
    }

    // TODO: Something better than exact string matching
    private fun buildArtist(
        media: MediaDetailsQuery.Data.Media,
        artist: AnimeTheme.Song.Artist,
    ): AnimeSongEntry.Artist {
        val edges = media.characters?.edges?.filterNotNull()
        val edge = edges?.find {
            val characterName = it.node.name ?: return@find false
            characterName.full == artist.character ||
                    (characterName.alternative?.any { it == artist.character } == true)
        }

        val voiceActor = edges
            ?.flatMap { it.voiceActors?.filterNotNull().orEmpty() }
            ?.firstOrNull {
                it.name?.full == artist.name ||
                        (it.name?.alternative?.any { it == artist.name } == true)
            }

        // If character search failed, but voiceActor succeeded, try to find the character again
        val characterEdge = edge ?: voiceActor?.let {
            edges.find {
                it.voiceActors?.contains(voiceActor) == true
            }
        }

        val character = characterEdge?.node?.let {
            AnimeSongEntry.Artist.Character(
                aniListId = it.id.toString(),
                image = it.image?.large,
                name = it.name?.userPreferred ?: artist.character ?: "",
            )
        }

        val artistImage = voiceActor?.image?.large
            ?: (artist.images.firstOrNull {
                it.facet == AnimeTheme.Song.Artist.Images.Facet.SmallCover
            } ?: artist.images.firstOrNull())?.link

        return AnimeSongEntry.Artist(
            id = artist.id,
            aniListId = voiceActor?.id?.toString(),
            animeThemesSlug = artist.slug,
            name = artist.name,
            image = artistImage,
            asCharacter = !artist.character.isNullOrBlank(),
            character = character,
        )
    }

    fun getAnimeSongState(animeSongId: String) = animeSongStates[animeSongId]!!

    fun onAnimeSongPlayAudioClick(animeSongId: String) {
        val state = animeSongStates[animeSongId]!!
        mediaPlayer.playPause(state.id, state.entry.audioUrl!!)
        animeSongStates.forEach { it.value.setExpanded(false) }
    }

    fun onAnimeSongProgressUpdate(animeSongId: String, progress: Float) {
        mediaPlayer.updateProgress(animeSongId, progress)
    }

    fun onAnimeSongExpandedToggle(animeSongId: String, expanded: Boolean) {
        animeSongStates.forEach {
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
        animeSongStates.forEach { mediaPlayer.pause(it.key) }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        animeSongStates.forEach { mediaPlayer.pause(it.key) }
    }

    override fun onCleared() {
        animeSongStates.forEach { mediaPlayer.stop(it.key) }
        super.onCleared()
    }

    fun animeSongsCollapseAll() {
        animeSongStates.forEach { it.value.setExpanded(false) }
        mediaPlayer.pause(null)
    }

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry<*>) =
        ignoreList.toggle(entry.media.id.toString())

    data class AnimeSongs(
        val entries: List<AnimeSongEntry>,
    )

    data class AnimeSongEntry(
        val id: String,
        val type: AnimeTheme.Type?,
        val title: String,
        val spoiler: Boolean,
        val artists: List<Artist>,
        val episodes: String?,
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
