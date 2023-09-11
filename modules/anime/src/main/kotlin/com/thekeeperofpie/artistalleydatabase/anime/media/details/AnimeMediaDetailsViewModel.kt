package com.thekeeperofpie.artistalleydatabase.anime.media.details

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.MediaDetailsQuery
import com.anilist.fragment.ListActivityMediaListActivityItem
import com.anilist.type.MediaType
import com.anilist.type.RecommendationRating
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.foldPreviousResult
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.AppMediaPlayer
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.character.DetailsCharacter
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumThreadSortOption
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadEntry
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadStatusController
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.history.HistoryController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toFavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationData
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongEntry
import com.thekeeperofpie.artistalleydatabase.anime.songs.AnimeSongsProvider
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import com.thekeeperofpie.artistalleydatabase.anime.utils.toStableMarkdown
import com.thekeeperofpie.artistalleydatabase.cds.data.CdEntryDao
import com.thekeeperofpie.artistalleydatabase.cds.grid.CdEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.StableSpanned
import dagger.hilt.android.lifecycle.HiltViewModel
import io.noties.markwon.Markwon
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.take
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
    val mediaListStatusController: MediaListStatusController,
    private val recommendationStatusController: RecommendationStatusController,
    val ignoreController: IgnoreController,
    val settings: AnimeSettings,
    favoritesController: FavoritesController,
    private val activityStatusController: ActivityStatusController,
    private val threadStatusController: ForumThreadStatusController,
    private val historyController: HistoryController,
    private val markwon: Markwon,
) : ViewModel(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AnimeMediaDetailsViewModel"
    }

    val screenKey = "${AnimeNavDestinations.MEDIA_DETAILS.id}-${UUID.randomUUID()}"
    val viewer = aniListApi.authedUser
    lateinit var mediaId: String

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    val threadToggleHelper =
        ForumThreadToggleHelper(aniListApi, threadStatusController, viewModelScope)

    val recommendationToggleHelper =
        RecommendationToggleHelper(aniListApi, recommendationStatusController, viewModelScope)

    val hasAuth = oAuthStore.hasAuth

    val refresh = MutableStateFlow(-1L)
    private val refreshSecondary = MutableStateFlow(-1L)

    var entry by mutableStateOf<LoadingResult<AnimeMediaDetailsScreen.Entry>>(LoadingResult.loading())
    var entry2 by mutableStateOf<LoadingResult<AnimeMediaDetailsScreen.Entry2>>(LoadingResult.loading())
    var listStatus by mutableStateOf<MediaListStatusController.Update?>(null)
    var forumThreads by mutableStateOf<ImmutableList<ForumThreadEntry>?>(null)
    val charactersDeferred = MutableStateFlow(PagingData.empty<DetailsCharacter>())
    val staff = MutableStateFlow(PagingData.empty<DetailsStaff>())

    var animeSongs by mutableStateOf<AnimeSongs?>(null)
    var cdEntries by mutableStateOf(emptyList<CdEntryGridModel>())

    var trailerPlaybackPosition = 0f

    var activities by mutableStateOf<ActivitiesEntry?>(null)

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
            refresh.flatMapLatest { aniListApi.mediaDetails(mediaId) }
                .mapLatest {
                    val result = it.result
                    if (result != null && result.media?.isAdult != false
                        && !settings.showAdult.value
                    ) {
                        LoadingResult.error(
                            R.string.anime_media_details_error_loading,
                            IOException("Cannot load media")
                        )
                    } else {
                        it
                    }
                }
                .onEach {
                    it.result?.media?.let {
                        historyController.onVisitMediaDetails(
                            mediaId = it.id.toString(),
                            type = it.type,
                            isAdult = it.isAdult,
                            bannerImage = it.bannerImage,
                            coverImage = it.coverImage?.extraLarge,
                            titleRomaji = it.title?.romaji,
                            titleEnglish = it.title?.english,
                            titleNative = it.title?.native,
                        )
                    }
                }
                .runningFold(null as Pair<Pair<String?, StableSpanned?>?, LoadingResult<MediaDetailsQuery.Data>>?) { accumulator, loadingResult ->
                    val descriptionRaw = loadingResult.result?.media?.description
                    val previousDescriptionPair = accumulator?.first
                    val description =
                        if (previousDescriptionPair != null && descriptionRaw == previousDescriptionPair.first) {
                            previousDescriptionPair.second
                        } else {
                            descriptionRaw?.let(markwon::toStableMarkdown)
                        }
                    descriptionRaw to description to loadingResult
                }
                .filterNotNull()
                .flatMapLatest { (descriptionPair, loadingResult) ->
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
                                    MediaPreviewEntry(node)
                                )
                            }
                            .orEmpty()
                            .sortedBy { AnimeMediaDetailsScreen.RELATION_SORT_ORDER.indexOf(it.relation) }

                        val mediaIds = setOf(media.id.toString()) +
                                relations.map { it.entry.media.id.toString() }

                        combine(
                            mediaListStatusController.allChanges(mediaIds),
                            ignoreController.updates(),
                            settings.showAdult,
                            settings.showLessImportantTags,
                            settings.showSpoilerTags,
                        ) { mediaListUpdates, _, showAdult, showLessImportantTags, showSpoilerTags ->
                            loadingResult.transformResult {
                                AnimeMediaDetailsScreen.Entry(
                                    mediaId,
                                    media,
                                    relations = relations.mapNotNull {
                                        applyMediaFiltering(
                                            statuses = mediaListUpdates,
                                            ignoreController = ignoreController,
                                            showAdult = showAdult,
                                            showIgnored = true,
                                            showLessImportantTags = showLessImportantTags,
                                            showSpoilerTags = showSpoilerTags,
                                            entry = it,
                                            transform = { it.entry },
                                            media = it.entry.media,
                                            copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                                copy(
                                                    entry = entry.copy(
                                                        mediaListStatus = mediaListStatus,
                                                        progress = progress,
                                                        progressVolumes = progressVolumes,
                                                        scoreRaw = scoreRaw,
                                                        ignored = ignored,
                                                        showLessImportantTags = showLessImportantTags,
                                                        showSpoilerTags = showSpoilerTags,
                                                    )
                                                )
                                            }
                                        )
                                    }.toImmutableList(),
                                    description = descriptionPair?.second,
                                )
                            }
                        }
                    }
                }
                .foldPreviousResult()
                .catch { emit(LoadingResult.error(R.string.anime_media_details_error_loading, it)) }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            combine(
                snapshotFlow { entry.result }.filterNotNull().flowOn(CustomDispatchers.Main),
                mediaListStatusController.allChanges(mediaId),
            ) { entry, update ->
                val mediaListEntry = entry.media.mediaListEntry
                MediaListStatusController.Update(
                    mediaId = entry.mediaId,
                    entry = if (update == null) mediaListEntry else update.entry,
                )
            }
                .catch {}
                .flowOn(CustomDispatchers.IO)
                .collectLatest { listStatus = it }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry.result?.media?.characters }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { characters ->
                    AniListPager(perPage = 5) { page ->
                        if (page == 1) {
                            characters.run { pageInfo to edges }
                        } else {
                            aniListApi.mediaDetailsCharactersPage(mediaId, page).characters
                                .run { pageInfo to edges }
                        }
                    }
                        .mapLatest { it.mapOnIO(CharacterUtils::toDetailsCharacter) }
                }
                .enforceUniqueIds { it.id }
                .cachedIn(viewModelScope)
                .collectLatest(charactersDeferred::emit)
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry.result }
                .filterNotNull()
                .take(1)
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest {
                    refresh.flatMapLatest {
                        AniListPager {
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
                                val songEntries =
                                    animeSongsProvider.getSongs(media).toImmutableList()
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
            combine(
                aniListApi.authedUser,
                snapshotFlow { entry.result }.flowOn(CustomDispatchers.Main)
                    .filterNotNull(),
                ::Pair
            )
                .mapLatest { (viewer, entry) ->
                    val result = aniListApi.mediaDetailsActivity(
                        mediaId = entry.mediaId,
                        includeFollowing = viewer != null,
                    )
                    ActivitiesEntry(
                        following = result.following?.activities
                            ?.filterIsInstance<ListActivityMediaListActivityItem>()
                            .orEmpty()
                            .map(::ActivityEntry)
                            .toImmutableList(),
                        global = result.global?.activities
                            ?.filterIsInstance<ListActivityMediaListActivityItem>()
                            .orEmpty()
                            .map(::ActivityEntry)
                            .toImmutableList(),
                    )
                }
                .flatMapLatest { activities ->
                    activityStatusController.allChanges(
                        (activities.following.map { it.activityId }
                                + activities.global.map { it.activityId })
                            .toSet()
                    )
                        .mapLatest { updates ->
                            activities.copy(
                                following = activities.following.map {
                                    it.copy(
                                        liked = updates[it.activityId]?.liked ?: it.liked,
                                        subscribed = updates[it.activityId]?.subscribed
                                            ?: it.subscribed,
                                    )
                                }.toImmutableList(),
                                global = activities.global.map {
                                    it.copy(
                                        liked = updates[it.activityId]?.liked ?: it.liked,
                                        subscribed = updates[it.activityId]?.subscribed
                                            ?: it.subscribed,
                                    )
                                }.toImmutableList(),
                            )
                        }
                }
                .catch {}
                .flowOn(CustomDispatchers.IO)
                .collectLatest { activities = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { entry.result }
                .filterNotNull()
                .take(1)
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest {
                    refresh.mapLatest {
                        aniListApi.forumThreadSearch(
                            null,
                            false,
                            null,
                            mediaCategoryId = mediaId,
                            sort = ForumThreadSortOption.REPLIED_AT.toApiValue(sortAscending = false),
                            page = 1,
                        ).page.threads?.filterNotNull().orEmpty()
                            .map { ForumThreadEntry(thread = it, bodyMarkdown = null) }
                    }
                }
                .flatMapLatest { threads ->
                    threadStatusController.allChanges(threads.map { it.thread.id.toString() }
                        .toSet())
                        .mapLatest { updates ->
                            threads.map {
                                val update = updates[it.thread.id.toString()]
                                val liked = update?.liked ?: it.liked
                                val subscribed = update?.subscribed ?: it.subscribed
                                it.transformIf(liked != it.liked || subscribed != it.subscribed) {
                                    copy(liked = liked, subscribed = subscribed)
                                }
                            }.toImmutableList()
                        }
                }
                .catch {}
                .collectLatest { forumThreads = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { entry.result }
                .filterNotNull()
                .take(1)
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest {
                    combine(refresh, refreshSecondary, ::Pair)
                        .flatMapLatest { aniListApi.mediaDetails2(mediaId) }
                        .flatMapLatest { result ->
                            val media = result.result?.media
                            if (media == null) {
                                flowOf(result.transformResult<AnimeMediaDetailsScreen.Entry2> { null })
                            } else {
                                val recommendations = media.recommendations?.edges?.filterNotNull()
                                    ?.mapNotNull {
                                        val node = it.node ?: return@mapNotNull null
                                        val mediaRecommendation = node.mediaRecommendation
                                        AnimeMediaDetailsScreen.Entry2.Recommendation(
                                            node.id.toString(),
                                            RecommendationData(
                                                mediaId = mediaId,
                                                recommendationMediaId = mediaRecommendation.id.toString(),
                                                rating = node.rating ?: 0,
                                                userRating = node.userRating
                                                    ?: RecommendationRating.NO_RATING,
                                            ),
                                            MediaPreviewEntry(mediaRecommendation)
                                        )
                                    }
                                    .orEmpty()

                                val recommendationMediaIds =
                                    recommendations.map { it.entry.media.id.toString() }.toSet()

                                combine(
                                    mediaListStatusController.allChanges(recommendationMediaIds),
                                    recommendationStatusController.allChanges(
                                        mediaId,
                                        recommendationMediaIds,
                                    ),
                                    ignoreController.updates(),
                                    settings.showAdult,
                                    settings.showLessImportantTags,
                                    settings.showSpoilerTags,
                                ) { mediaListUpdates, recommendationUpdates, _, showAdult, showLessImportantTags, showSpoilerTags ->
                                    result.transformResult {
                                        AnimeMediaDetailsScreen.Entry2(
                                            mediaId,
                                            media,
                                            recommendations = recommendations.mapNotNull {
                                                applyMediaFiltering(
                                                    statuses = mediaListUpdates,
                                                    ignoreController = ignoreController,
                                                    showAdult = showAdult,
                                                    showIgnored = true,
                                                    showLessImportantTags = showLessImportantTags,
                                                    showSpoilerTags = showSpoilerTags,
                                                    entry = it,
                                                    transform = { it.entry },
                                                    media = it.entry.media,
                                                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                                        copy(
                                                            entry = entry.copy(
                                                                media = it.entry.media,
                                                                mediaListStatus = mediaListStatus,
                                                                progress = progress,
                                                                progressVolumes = progressVolumes,
                                                                scoreRaw = scoreRaw,
                                                                ignored = ignored,
                                                                showLessImportantTags = showLessImportantTags,
                                                                showSpoilerTags = showSpoilerTags,
                                                            )
                                                        )
                                                    }
                                                )?.let {
                                                    val recommendationUpdate =
                                                        recommendationUpdates[it.data.mediaId to it.data.recommendationMediaId]
                                                    val userRating = recommendationUpdate?.rating
                                                        ?: it.data.userRating
                                                    it.transformIf(userRating != it.data.userRating) {
                                                        copy(
                                                            data = data.copy(
                                                                userRating = userRating
                                                            )
                                                        )
                                                    }
                                                }
                                            }.toImmutableList(),
                                        )
                                    }
                                }
                            }
                        }
                }
                .foldPreviousResult()
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry2 = it }
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

    fun refreshSecondary() {
        refreshSecondary.value = SystemClock.uptimeMillis()
    }

    fun animeSongsCollapseAll() {
        animeSongStates.forEach { it.value.setExpanded(false) }
        mediaPlayer.pause(null)
    }

    data class AnimeSongs(
        val entries: ImmutableList<AnimeSongEntry>,
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

    data class ActivitiesEntry(
        val following: ImmutableList<ActivityEntry>,
        val global: ImmutableList<ActivityEntry>,
    )

    data class ActivityEntry(
        val activity: ListActivityMediaListActivityItem,
        val activityId: String = activity.id.toString(),
        override val liked: Boolean = false,
        override val subscribed: Boolean = false,
    ) : ActivityStatusAware
}
