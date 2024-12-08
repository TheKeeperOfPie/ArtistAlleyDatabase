package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_error_loading
import com.anilist.data.MediaTitlesAndImagesQuery
import com.anilist.data.ToggleFollowMutation
import com.anilist.data.UserSocialActivityQuery
import com.anilist.data.fragment.PaginationInfo
import com.anilist.data.fragment.UserFavoriteMediaNode
import com.anilist.data.fragment.UserMediaStatistics
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityEntry
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivitySortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivitySortOption
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activities.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activities.applyActivityFiltering
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.data.toNextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaFilterableData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toMediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
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
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AniListUserViewModel(
    private val aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val activityStatusController: ActivityStatusController,
    private val ignoreController: IgnoreController,
    private val settings: AnimeSettings,
    private val markdown: Markdown,
    featureOverrideProvider: FeatureOverrideProvider,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaDetailsRoute: MediaDetailsRoute,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<AnimeDestination.User>(navigationTypeMap)
    val userId = destination.userId

    var entry by mutableStateOf<AniListUserScreen.Entry?>(null)
    val viewer = aniListApi.authedUser
    var errorResource by mutableStateOf<Pair<StringResource, Exception?>?>(null)

    val anime = MutableStateFlow(PagingData.empty<MediaEntry>())
    val manga = MutableStateFlow(PagingData.empty<MediaEntry>())
    val characters = MutableStateFlow(PagingData.empty<CharacterDetails>())
    val staff = MutableStateFlow(PagingData.empty<StaffDetails>())
    var studios by mutableStateOf(StudiosEntry())
        private set

    val activities = MutableStateFlow(PagingData.empty<ActivityEntry<MediaCompactWithTagsEntry>>())

    val animeStats = States.Anime(viewModelScope, aniListApi)
    val mangaStats = States.Manga(viewModelScope, aniListApi)

    val activitySortFilterController = ActivitySortFilterController(
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        // Disable shared element otherwise the tab view will animate into the sort list
        mediaSharedElement = false,
        mediaDetailsRoute = mediaDetailsRoute,
    )

    val activityToggleHelper =
        ActivityToggleHelper(aniListApi, activityStatusController, viewModelScope)

    private val timeZone = TimeZone.currentSystemDefault()
    private var toggleFollowRequestMillis = MutableStateFlow(-1L)
    private var initialFollowState by mutableStateOf<Boolean?>(null)
    private var toggleFollowingResult by mutableStateOf<ToggleFollowMutation.Data.ToggleFollow?>(
        null
    )

    val isFollowing: Boolean
        get() = initialFollowState ?: toggleFollowingResult?.isFollowing ?: entry?.user?.isFollowing
        ?: false

    private val refresh = RefreshFlow()

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(refresh.updates, aniListApi.authedUser, ::Pair)
                .collectLatest { (_, viewer) ->
                    withContext(CustomDispatchers.Main) {
                        entry = null
                    }
                    try {
                        val userOrViewerId = userId ?: viewer?.id
                        if (userOrViewerId == null) {
                            withContext(CustomDispatchers.Main) {
                                errorResource = Res.string.anime_media_list_error_loading to null
                            }
                        } else {
                            entry = aniListApi.user(userOrViewerId)
                                ?.let {
                                    val about = it.about?.let(markdown::convertMarkdownText)
                                    AniListUserScreen.Entry(it, about)
                                }
                        }
                    } catch (e: Exception) {
                        withContext(CustomDispatchers.Main) {
                            errorResource = Res.string.anime_media_list_error_loading to e
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
                    mediaFilteringData = settings.mediaFilteringData(false),
                    mediaFilterable = { it.mediaFilterable },
                    copy = { copy(mediaFilterable = it) },
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
                    mediaFilteringData = settings.mediaFilteringData(false),
                    mediaFilterable = { it.mediaFilterable },
                    copy = { copy(mediaFilterable = it) },
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
                StaffDetails(
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
                            media = (it.main?.nodes?.filterNotNull().orEmpty()
                                .map(::MediaWithListStatusEntry) +
                                    it.nonMain?.nodes?.filterNotNull().orEmpty()
                                        .map(::MediaWithListStatusEntry))
                                .distinctBy { it.media.id }
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
                        refresh.updates,
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
                                    ?.atStartOfDayIn(timeZone)
                                    ?.epochSeconds
                                    ?.toInt(),
                                createdAtLesser = filterParams.date.endDate
                                    ?.plus(1, DateTimeUnit.DAY)
                                    ?.atStartOfDayIn(timeZone)
                                    ?.epochSeconds
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
                .mapLatest {
                    it.mapOnIO {
                        ActivityEntry(it, MediaCompactWithTagsEntry.Provider)
                    }
                }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    combine(
                        mediaListStatusController.allChanges(),
                        activityStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { mediaListStatuses, activityStatuses, _, filteringData ->
                        pagingData.mapNotNull {
                            applyActivityFiltering(
                                mediaListStatuses = mediaListStatuses,
                                activityStatuses = activityStatuses,
                                ignoreController = ignoreController,
                                filteringData = filteringData,
                                entry = it,
                                activityId = it.activityId.valueId,
                                activityStatusAware = it,
                                media = (it.activity as? UserSocialActivityQuery.Data.Page.ListActivityActivity)?.media,
                                mediaFilterable = it.media?.mediaFilterable,
                                copyMedia = { copy(media = media?.copy(mediaFilterable = it)) },
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

    fun refresh() = refresh.refresh()

    fun logOut() = aniListApi.logOut()

    fun toggleFollow() {
        userId ?: return
        initialFollowState = entry?.user?.isFollowing?.not() ?: true
        toggleFollowRequestMillis.value = Clock.System.now().toEpochMilliseconds()
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
        val mediaFilterable: MediaFilterableData = MediaFilterableData(
            mediaId = userMedia.id.toString(),
            isAdult = userMedia.isAdult,
            mediaListStatus = userMedia.mediaListEntry?.status?.toMediaListStatus(),
            progress = userMedia.mediaListEntry?.progress,
            progressVolumes = userMedia.mediaListEntry?.progressVolumes,
            scoreRaw = userMedia.mediaListEntry?.score,
            ignored = false,
            showLessImportantTags = false,
            showSpoilerTags = false,
        ),
    ) : MediaGridCard.Entry {
        override val media get() = userMedia
        override val type get() = userMedia.type
        override val color = userMedia.coverImage?.color?.let(ComposeColorUtils::hexToColor)
        override val averageScore get() = userMedia.averageScore
        override val chapters get() = userMedia.chapters
        override val episodes get() = userMedia.episodes
        override val volumes get() = userMedia.volumes
        override val nextAiringEpisode get() = userMedia.nextAiringEpisode?.toNextAiringEpisode()
        override val ignored get() = mediaFilterable.ignored
        override val mediaListStatus get() = mediaFilterable.mediaListStatus
        override val progress get() = mediaFilterable.progress
        override val progressVolumes get() = mediaFilterable.progressVolumes
        override val scoreRaw get() = mediaFilterable.scoreRaw
    }

    data class StudiosEntry(
        val hasMore: Boolean = false,
        val studios: List<StudioListRow.Entry> = emptyList(),
    )
}
