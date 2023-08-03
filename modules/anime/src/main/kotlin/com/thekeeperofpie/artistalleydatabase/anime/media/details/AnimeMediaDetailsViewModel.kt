package com.thekeeperofpie.artistalleydatabase.anime.media.details

import android.app.Application
import android.os.SystemClock
import android.util.Log
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
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.anilist.MediaActivityPageQuery
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.foldPreviousResult
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.AppMediaPlayer
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivitySortOption
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.character.DetailsCharacter
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongEntry
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsProvider
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Optional
import java.util.UUID
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalCoroutinesApi::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@HiltViewModel
class AnimeMediaDetailsViewModel @Inject constructor(
    private val application: Application,
    private val aniListApi: AuthedAniListApi,
    private val cdEntryDao: CdEntryDao,
    private val appJson: AppJson,
    private val animeSongsProviderOptional: Optional<AnimeSongsProvider>,
    val mediaPlayer: AppMediaPlayer,
    oAuthStore: AniListOAuthStore,
    val statusController: MediaListStatusController,
    val ignoreList: AnimeMediaIgnoreList,
    val settings: AnimeSettings,
    favoritesController: FavoritesController,
    private val activityStatusController: ActivityStatusController,
) : ViewModel(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AnimeMediaDetailsViewModel"
        private var counter = 0
    }

    val screenKey = "${AnimeNavDestinations.MEDIA_DETAILS.id}-${UUID.randomUUID()}"
    val viewer = aniListApi.authedUser
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    lateinit var mediaId: String

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    val hasAuth = oAuthStore.hasAuth

    val refresh = MutableStateFlow(-1L)

    var entry by mutableStateOf<LoadingResult<AnimeMediaDetailsScreen.Entry>>(LoadingResult.loading())
    var listStatus by mutableStateOf<MediaListStatusController.Update?>(null)
    var activities by mutableStateOf<List<ActivityEntry>?>(null)
    val charactersDeferred = MutableStateFlow(PagingData.empty<DetailsCharacter>())
    val staff = MutableStateFlow(PagingData.empty<DetailsStaff>())

    var animeSongs by mutableStateOf<AnimeSongs?>(null)
    var cdEntries by mutableStateOf(emptyList<CdEntryGridModel>())

    var trailerPlaybackPosition = 0f

    private var animeSongStates by mutableStateOf(emptyMap<String, AnimeSongState>())

    fun initialize(mediaId: String) {
        if (::mediaId.isInitialized) return
        this.mediaId = mediaId
        favoritesToggleHelper.initializeTracking(
            viewModel = this,
            entry = { snapshotFlow { entry.result } },
            entryToId = { it.mediaId },
            entryToType = { it.media.type.toFavoriteType() },
            entryToFavorite = { it.media.isFavourite },
        )

        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.flatMapLatest {
                aniListApi.mediaDetails(mediaId)
                    .map {
                        val result = it.result
                        if (result != null && result.media?.isAdult != false
                            && !settings.showAdult.value
                        ) {
                            throw IOException("Cannot load media")
                        }
                        it
                    }
                    .flatMapLatest { loadingResult ->
                        val media = loadingResult.result?.media
                        if (media == null) {
                            flowOf(loadingResult
                                .transformResult<AnimeMediaDetailsScreen.Entry> { null })
                        } else {
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
                                    val mediaRecommendation =
                                        node.mediaRecommendation ?: return@mapNotNull null
                                    AnimeMediaDetailsScreen.Entry.Recommendation(
                                        node.id.toString(),
                                        node.rating,
                                        AnimeMediaListRow.Entry(mediaRecommendation)
                                    )
                                }
                                .orEmpty()

                            val mediaIds = setOf(media.id.toString()) +
                                    relations.map { it.entry.media.id.toString() } +
                                    recommendations.map { it.entry.media.id.toString() }
                            combine(
                                statusController.allChanges(mediaIds),
                                ignoreList.updates,
                                settings.showAdult,
                                settings.showLessImportantTags,
                                settings.showSpoilerTags,
                            ) { statuses, ignoredIds, showAdult, showLessImportantTags, showSpoilerTags ->
                                loadingResult.transformResult {
                                    AnimeMediaDetailsScreen.Entry(
                                        mediaId,
                                        media,
                                        relations = relations.mapNotNull {
                                            applyMediaFiltering(
                                                statuses = statuses,
                                                ignoredIds = ignoredIds,
                                                showAdult = showAdult,
                                                showIgnored = true,
                                                showLessImportantTags = showLessImportantTags,
                                                showSpoilerTags = showSpoilerTags,
                                                entry = it,
                                                transform = { it.entry },
                                                media = it.entry.media,
                                                copy = { mediaListStatus, progress, progressVolumes, ignored, showLessImportantTags, showSpoilerTags ->
                                                    copy(
                                                        entry = AnimeMediaListRow.Entry(
                                                            media = it.entry.media,
                                                            mediaListStatus = mediaListStatus,
                                                            progress = progress,
                                                            progressVolumes = progressVolumes,
                                                            ignored = ignored,
                                                            showLessImportantTags = showLessImportantTags,
                                                            showSpoilerTags = showSpoilerTags,
                                                        )
                                                    )
                                                }
                                            )
                                        },
                                        recommendations = recommendations.mapNotNull {
                                            applyMediaFiltering(
                                                statuses = statuses,
                                                ignoredIds = ignoredIds,
                                                showAdult = showAdult,
                                                showIgnored = true,
                                                showLessImportantTags = showLessImportantTags,
                                                showSpoilerTags = showSpoilerTags,
                                                entry = it,
                                                transform = { it.entry },
                                                media = it.entry.media,
                                                copy = { mediaListStatus, progress, progressVolumes, ignored, showLessImportantTags, showSpoilerTags ->
                                                    copy(
                                                        entry = AnimeMediaListRow.Entry(
                                                            media = it.entry.media,
                                                            mediaListStatus = mediaListStatus,
                                                            progress = progress,
                                                            progressVolumes = progressVolumes,
                                                            ignored = ignored,
                                                            showLessImportantTags = showLessImportantTags,
                                                            showSpoilerTags = showSpoilerTags,
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
            }
                .foldPreviousResult()
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry.result }
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
            snapshotFlow { entry.result?.media?.let { it.id.toString() to it.characters } }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { (mediaId, characters) ->
                    Pager(config = PagingConfig(10)) {
                        AniListPagingSource { page ->
                            if (page == 1) {
                                characters?.pageInfo to characters?.edges?.filterNotNull()
                                    .orEmpty()
                            } else {
                                val result =
                                    aniListApi.mediaDetailsCharactersPage(
                                        mediaId,
                                        page
                                    ).characters
                                result.pageInfo to result.edges.filterNotNull()
                            }
                        }
                    }.flow
                }
                .mapLatest { it.map(CharacterUtils::toDetailsCharacter) }
                .enforceUniqueIds { it.id }
                .cachedIn(viewModelScope)
                .collectLatest(charactersDeferred::emit)
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry.result?.media?.let { it.id.toString() to it.staff } }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { (mediaId, staff) ->
                    Pager(config = PagingConfig(10)) {
                        AniListPagingSource {
                            if (it == 1) {
                                staff?.pageInfo to staff?.edges?.filterNotNull().orEmpty()
                                    .mapNotNull {
                                        val role = it.role
                                        it.node?.let {
                                            DetailsStaff(
                                                id = it.id.toString(),
                                                name = it.name,
                                                image = it.image?.large,
                                                role = role,
                                                staff = it
                                            )
                                        }
                                    }
                            } else {
                                val result =
                                    aniListApi.mediaDetailsStaffPage(mediaId, it).staff
                                result.pageInfo to result.edges.filterNotNull().map {
                                    DetailsStaff(
                                        id = it.node.id.toString(),
                                        name = it.node.name,
                                        image = it.node.image?.large,
                                        role = it.role,
                                        staff = it.node,
                                    )
                                }
                            }
                        }
                    }.flow
                }
                .enforceUniqueIds { it.idWithRole }
                .cachedIn(viewModelScope)
                .collectLatest(staff::emit)
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            val cdEntries = cdEntryDao.searchSeriesByMediaId(appJson, mediaId)
                .map { CdEntryGridModel.buildFromEntry(application, it) }
            withContext(CustomDispatchers.Main) {
                this@AnimeMediaDetailsViewModel.cdEntries = cdEntries
            }
        }

        val animeSongsProvider = animeSongsProviderOptional.getOrNull()
        if (animeSongsProvider != null) {
            viewModelScope.launch(CustomDispatchers.IO) {
                snapshotFlow { entry }
                    .flowOn(CustomDispatchers.Main)
                    .collectLatest {
                        val media = it.result?.media
                        if (media?.type == MediaType.ANIME) {
                            try {
                                val songEntries = animeSongsProvider.getSongs(media)
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

        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { entry.result }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .mapLatest {
                    aniListApi.mediaActivitiesPage(
                        id = it.mediaId,
                        page = 1,
                        sort = ActivitySortOption.PINNED.toApiValue(),
                        activitiesPerPage = 10,
                    )
                        .page.activities
                        .filterIsInstance<MediaActivityPageQuery.Data.Page.ListActivityActivity>()
                }
                .flatMapLatest { activities ->
                    activityStatusController.allChanges(activities.map { it.id.toString() }
                        .toSet())
                        .mapLatest { updates ->
                            activities.map {
                                ActivityEntry(
                                    it,
                                    liked = updates[it.id.toString()]?.liked ?: it.isLiked
                                    ?: false,
                                    subscribed = updates[it.id.toString()]?.subscribed
                                        ?: it.isSubscribed ?: false,
                                )
                            }
                        }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { activities = it }
        }
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

    fun refresh() {
        refresh.value = SystemClock.uptimeMillis()
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

    data class ActivityEntry(
        val activity: MediaActivityPageQuery.Data.Page.ListActivityActivity,
        val activityId: String = activity.id.toString(),
        override val liked: Boolean,
        override val subscribed: Boolean,
    ) : ActivityStatusAware
}
