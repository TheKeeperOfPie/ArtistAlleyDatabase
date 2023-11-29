package com.thekeeperofpie.artistalleydatabase.anime.user

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.MediaTitlesAndImagesQuery
import com.anilist.ToggleFollowMutation
import com.anilist.UserSocialActivityQuery
import com.anilist.fragment.PaginationInfo
import com.anilist.fragment.UserFavoriteMediaNode
import com.anilist.fragment.UserMediaStatistics
import com.anilist.type.MediaListStatus
import com.hoc081098.flowext.combine
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityEntry
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivitySortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivitySortOption
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activity.applyActivityFiltering
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.character.DetailsCharacter
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.anime.staff.DetailsStaff
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import com.thekeeperofpie.artistalleydatabase.anime.utils.toStableMarkdown
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import dagger.hilt.android.lifecycle.HiltViewModel
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AniListUserViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val activityStatusController: ActivityStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    private val markwon: Markwon,
    featureOverrideProvider: FeatureOverrideProvider,
) : ViewModel() {

    val screenKey = "${AnimeNavDestinations.USER.id}-${UUID.randomUUID()}"
    private var initialized = false
    var userId: String? = null

    var entry by mutableStateOf<AniListUserScreen.Entry?>(null)
    val viewer = aniListApi.authedUser
    var errorResource by mutableStateOf<Pair<Int, Exception?>?>(null)

    val anime = MutableStateFlow(PagingData.empty<MediaEntry>())
    val manga = MutableStateFlow(PagingData.empty<MediaEntry>())
    val characters = MutableStateFlow(PagingData.empty<DetailsCharacter>())
    val staff = MutableStateFlow(PagingData.empty<DetailsStaff>())
    var studios by mutableStateOf(StudiosEntry())
        private set

    val activities = MutableStateFlow(PagingData.empty<ActivityEntry>())

    val animeStats = States.Anime(viewModelScope, aniListApi)
    val mangaStats = States.Manga(viewModelScope, aniListApi)

    val activitySortFilterController = ActivitySortFilterController(
        screenKey = AnimeNavDestinations.ACTIVITY.id,
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        // Disable shared element otherwise the tab view will animate into the sort list
        mediaSharedElement = false,
    )

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    private val offset = ZoneId.systemDefault().rules.getOffset(Instant.now())
    private var toggleFollowRequestMillis = MutableStateFlow(-1L)
    private var initialFollowState by mutableStateOf<Boolean?>(null)
    private var toggleFollowingResult by mutableStateOf<ToggleFollowMutation.Data.ToggleFollow?>(
        null
    )

    val isFollowing: Boolean
        get() = initialFollowState ?: toggleFollowingResult?.isFollowing ?: entry?.user?.isFollowing
        ?: false

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(userId: String?) {
        if (initialized) return
        initialized = true
        this.userId = userId

        viewModelScope.launch(CustomDispatchers.IO) {
            combine(refreshUptimeMillis, aniListApi.authedUser, ::Pair)
                .collectLatest { (_, viewer) ->
                    withContext(CustomDispatchers.Main) {
                        entry = null
                    }
                    try {
                        val userOrViewerId = userId ?: viewer?.id
                        if (userOrViewerId == null) {
                            withContext(CustomDispatchers.Main) {
                                errorResource = R.string.anime_media_list_error_loading to null
                            }
                        } else {
                            entry = aniListApi.user(userOrViewerId)
                                ?.let {
                                    val about = it.about?.let(markwon::toStableMarkdown)
                                    AniListUserScreen.Entry(it, about)
                                }
                        }
                    } catch (e: Exception) {
                        withContext(CustomDispatchers.Main) {
                            errorResource = R.string.anime_media_list_error_loading to e
                        }
                    }
                }
        }

        // TODO: Better placeholders for loading horizontal scrolling rows
        collectFavoritesPage(
            request = { entry, page ->
                if (page == 1) {
                    val result = entry.user.favourites?.anime
                    result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
                } else {
                    val result = aniListApi.userDetailsAnimePage(
                        userId = entry.user.id.toString(),
                        page = page,
                    )
                    result.pageInfo to result.nodes.filterNotNull()
                }
            },
            map = ::MediaEntry,
            id = { it.media.id.toString() },
            property = anime,
            transformFlow = {
                applyMediaStatusChanges(
                    statusController = mediaListStatusController,
                    ignoreController = ignoreController,
                    settings = settings,
                    media = { it.media },
                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                        copy(
                            mediaListStatus = mediaListStatus,
                            progress = progress,
                            progressVolumes = progressVolumes,
                            scoreRaw = scoreRaw,
                            ignored = ignored,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                        )
                    },
                )
            }
        )

        collectFavoritesPage(
            request = { entry, page ->
                if (page == 1) {
                    val result = entry.user.favourites?.manga
                    result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
                } else {
                    val result = aniListApi.userDetailsMangaPage(
                        userId = entry.user.id.toString(),
                        page = page,
                    )
                    result.pageInfo to result.nodes.filterNotNull()
                }
            },
            map = ::MediaEntry,
            id = { it.media.id.toString() },
            property = manga,
            transformFlow = {
                applyMediaStatusChanges(
                    statusController = mediaListStatusController,
                    ignoreController = ignoreController,
                    settings = settings,
                    media = { it.media },
                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                        copy(
                            mediaListStatus = mediaListStatus,
                            progress = progress,
                            progressVolumes = progressVolumes,
                            scoreRaw = scoreRaw,
                            ignored = ignored,
                            showLessImportantTags = showLessImportantTags,
                            showSpoilerTags = showSpoilerTags,
                        )
                    },
                )
            }
        )

        collectFavoritesPage(
            request = { entry, page ->
                if (page == 1) {
                    val result = entry.user.favourites?.characters
                    result?.pageInfo to result?.edges?.filterNotNull().orEmpty()
                } else {
                    val result = aniListApi.userDetailsCharactersPage(
                        userId = entry.user.id.toString(),
                        page = page,
                    )
                    result.pageInfo to result.edges.filterNotNull()
                }
            },
            map = CharacterUtils::toDetailsCharacter,
            id = { it.id },
            property = characters,
        )

        collectFavoritesPage(
            request = { entry, page ->
                if (page == 1) {
                    val result = entry.user.favourites?.staff
                    result?.pageInfo to result?.nodes?.filterNotNull().orEmpty()
                        .map { it to it.primaryOccupations }
                } else {
                    val result = aniListApi.userDetailsStaffPage(
                        userId = entry.user.id.toString(),
                        page = page,
                    )
                    result.pageInfo to result.nodes.filterNotNull()
                        .map { it to it.primaryOccupations }
                }
            },
            map = { (staff, primaryOccupations) ->
                DetailsStaff(
                    id = staff.id.toString(),
                    name = staff.name,
                    image = staff.image?.large,
                    role = primaryOccupations?.filterNotNull()?.firstOrNull(),
                    staff = staff,
                )
            },
            id = { it.idWithRole },
            property = staff,
        )

        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { entry }
                .flowOn(CustomDispatchers.Main)
                .mapLatest {
                    val result = it?.user?.favourites?.studios
                    val hasMore = result?.pageInfo?.hasNextPage ?: false
                    val studios = result?.nodes?.filterNotNull()?.map {
                        StudioListRow.Entry(
                            studio = it,
                            media = it.main?.nodes?.filterNotNull().orEmpty()
                                .map(::MediaWithListStatusEntry) +
                                    it.nonMain?.nodes?.filterNotNull().orEmpty()
                                        .map(::MediaWithListStatusEntry)
                        )
                    }.orEmpty()
                    StudiosEntry(hasMore = hasMore, studios = studios)
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { studios = it }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            toggleFollowRequestMillis.filter { it > 0 }
                .mapLatest { aniListApi.toggleFollow(userId!!.toInt()) }
                .catch {
                    // TODO: Error message
                }
                .collect {
                    withContext(CustomDispatchers.Main) {
                        initialFollowState = null
                        toggleFollowingResult = it
                    }
                }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { (entry) ->
                    combine(
                        activitySortFilterController.filterParams,
                        refreshUptimeMillis,
                        ::Pair
                    ).flatMapLatest { (filterParams) ->
                        AniListPager {
                            val result = aniListApi.userSocialActivity(
                                isFollowing = null,
                                page = it,
                                userId = entry.id.toString(),
                                userIdNot = null,
                                sort = filterParams.sort
                                    .selectedOption(ActivitySortOption.NEWEST)
                                    .toApiValue(),
                                typeIn = filterParams.type
                                    .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                                    .map { it.value }
                                    .ifEmpty { null },
                                typeNotIn = filterParams.type
                                    .filter { it.state == FilterIncludeExcludeState.EXCLUDE }
                                    .map { it.value }
                                    .ifEmpty { null },
                                hasReplies = if (filterParams.hasReplies) true else null,
                                createdAtGreater = filterParams.date.startDate
                                    ?.atStartOfDay()
                                    ?.toEpochSecond(offset)
                                    ?.toInt(),
                                createdAtLesser = filterParams.date.endDate
                                    ?.plus(1, ChronoUnit.DAYS)
                                    ?.atStartOfDay()
                                    ?.toEpochSecond(offset)
                                    ?.toInt(),
                                mediaId = filterParams.mediaId,
                            )
                            result.page?.pageInfo to
                                    result.page?.activities?.filterNotNull().orEmpty()
                        }
                    }
                }
                .enforceUniqueIntIds {
                    when (it) {
                        is UserSocialActivityQuery.Data.Page.ListActivityActivity -> it.id
                        is UserSocialActivityQuery.Data.Page.MessageActivityActivity -> it.id
                        is UserSocialActivityQuery.Data.Page.TextActivityActivity -> it.id
                        is UserSocialActivityQuery.Data.Page.OtherActivity -> null
                    }
                }
                .mapLatest { it.mapOnIO(::ActivityEntry) }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    combine(
                        mediaListStatusController.allChanges(),
                        activityStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.showAdult,
                        settings.showIgnored,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { mediaListStatuses, activityStatuses, ignoredIds, showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
                        pagingData.mapNotNull {
                            applyActivityFiltering(
                                mediaListStatuses = mediaListStatuses,
                                activityStatuses = activityStatuses,
                                ignoreController = ignoreController,
                                showAdult = showAdult,
                                showIgnored = showIgnored,
                                showLessImportantTags = showLessImportantTags,
                                showSpoilerTags = showSpoilerTags,
                                entry = it,
                                activityId = it.activityId.valueId,
                                activityStatusAware = it,
                                media = (it.activity as? UserSocialActivityQuery.Data.Page.ListActivityActivity)?.media,
                                mediaStatusAware = it.media,
                                copyMedia = { status, progress, progressVolumes, ignored, showLessImportantTags, showSpoilerTags ->
                                    copy(
                                        media = media?.copy(
                                            mediaListStatus = status,
                                            progress = progress,
                                            progressVolumes = progressVolumes,
                                            ignored = ignored,
                                            showLessImportantTags = showLessImportantTags,
                                            showSpoilerTags = showSpoilerTags,
                                        )
                                    )
                                },
                                copyActivity = { liked, subscribed ->
                                    copy(liked = liked, subscribed = subscribed)
                                }
                            )
                        }
                    }
                }
                .cachedIn(viewModelScope)
                .collectLatest(activities::emit)
        }
    }

    private fun <ResponseType : Any, ResultType : Any> collectFavoritesPage(
        request: suspend (AniListUserScreen.Entry, page: Int) -> Pair<PaginationInfo?, List<ResponseType>>,
        map: (ResponseType) -> ResultType,
        id: (ResultType) -> String,
        property: MutableStateFlow<PagingData<ResultType>>,
        transformFlow: (Flow<PagingData<ResultType>>.() -> Flow<PagingData<ResultType>>)? = null,
    ) {
        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
                .flatMapLatest { entry -> AniListPager { request(entry, it) } }
                .mapLatest { it.mapOnIO(map) }
                .enforceUniqueIds(id)
                .cachedIn(viewModelScope)
                .run {
                    if (transformFlow == null) this else {
                        transformFlow().cachedIn(viewModelScope)
                    }
                }
                .collectLatest(property::emit)
        }
    }

    fun refresh() = refreshUptimeMillis.tryEmit(System.currentTimeMillis())

    fun logOut() = aniListApi.logOut()

    fun toggleFollow() {
        userId ?: return
        initialFollowState = entry?.user?.isFollowing?.not() ?: true
        toggleFollowRequestMillis.value = SystemClock.uptimeMillis()
    }

    sealed class States(
        private val viewModelScope: CoroutineScope,
        private val aniListApi: AuthedAniListApi,
    ) {
        val genresState = State<UserMediaStatistics.Genre>(
            { it.genre.orEmpty() },
            { it.mediaIds.filterNotNull() },
        )

        val tagsState = State<UserMediaStatistics.Tag>(
            { it.tag?.id.toString() },
            { it.mediaIds.filterNotNull() },
        )

        val staffState = State<UserMediaStatistics.Staff>(
            { it.staff?.id.toString() },
            { it.mediaIds.filterNotNull() },
        )

        @Composable
        fun <Value> getMedia(
            value: Value,
            state: State<Value>,
        ): Result<Map<Int, MediaTitlesAndImagesQuery.Data.Page.Medium>?> {
            val key = state.valueToKey(value)
            return state.mediaFlows.getOrPut(key) {
                state.refreshRequest.filter { it == key }
                    .startWith(key)
                    .flowOn(CustomDispatchers.IO)
                    .map {
                        Result.success(
                            aniListApi.mediaTitlesAndImages(state.valueToMediaIds(value))
                                .associateBy { it.id }
                        )
                    }
                    .catch { emit(Result.failure(it)) }
                    .shareIn(viewModelScope, started = SharingStarted.Lazily, replay = 1)
            }
                .collectAsState(initial = Result.success(null))
                .value
        }

        class Anime(viewModelScope: CoroutineScope, aniListApi: AuthedAniListApi) :
            States(viewModelScope, aniListApi) {
            val voiceActorsState = State<UserMediaStatistics.VoiceActor>(
                { it.voiceActor?.id.toString() },
                { it.mediaIds.filterNotNull() },
            )

            val studiosState = State<UserMediaStatistics.Studio>(
                { it.studio?.id.toString() },
                { it.mediaIds.filterNotNull() },
            )
        }

        class Manga(viewModelScope: CoroutineScope, aniListApi: AuthedAniListApi) :
            States(viewModelScope, aniListApi)

        inner class State<Value>(
            val valueToKey: (Value) -> String,
            val valueToMediaIds: (Value) -> List<Int>,
        ) {
            val refreshRequest = MutableStateFlow("")
            val mediaFlows =
                mutableMapOf<String, Flow<Result<Map<Int, MediaTitlesAndImagesQuery.Data.Page.Medium>>>>()

            @Composable
            fun getMedia(value: Value) = getMedia(value, this)
        }
    }

    data class MediaEntry(
        val userMedia: UserFavoriteMediaNode,
        override val mediaListStatus: MediaListStatus? = userMedia.mediaListEntry?.status,
        override val progress: Int? = MediaUtils.maxProgress(
            type = userMedia.type,
            chapters = userMedia.chapters,
            episodes = userMedia.episodes,
            nextAiringEpisode = userMedia.nextAiringEpisode?.episode,
        ),
        override val progressVolumes: Int? = userMedia.volumes,
        override val scoreRaw: Double? = userMedia.mediaListEntry?.score,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
    ) : MediaGridCard.Entry {
        override val media = userMedia
        override val type = userMedia.type
        override val maxProgress = MediaUtils.maxProgress(
            type = userMedia.type,
            chapters = userMedia.chapters,
            episodes = userMedia.episodes,
            nextAiringEpisode = userMedia.nextAiringEpisode?.episode,
        )
        override val maxProgressVolumes = userMedia.volumes
        override val averageScore = userMedia.averageScore
        override val color = userMedia.coverImage?.color?.let(ComposeColorUtils::hexToColor)
    }

    data class StudiosEntry(
        val hasMore: Boolean = false,
        val studios: List<StudioListRow.Entry> = emptyList(),
    )
}
