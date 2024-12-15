package com.thekeeperofpie.artistalleydatabase.anime.users

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import artistalleydatabase.modules.anime.users.generated.resources.Res
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_error_loading
import com.anilist.data.MediaTitlesAndImagesQuery
import com.anilist.data.ToggleFollowMutation
import com.anilist.data.UserSocialActivityQuery
import com.anilist.data.fragment.MediaCompactWithTags
import com.anilist.data.fragment.MediaWithListStatus
import com.anilist.data.fragment.PaginationInfo
import com.anilist.data.fragment.StudioListRowFragment
import com.anilist.data.fragment.UserMediaStatistics
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivitySortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivitySortOption
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityStatusController
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.applyActivityFiltering
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterUtils
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffDetails
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.StudioEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.users.stats.UserStatsDetailScreen
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.foldPreviousResult
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AniListUserViewModel<ActivityEntry : Any, MediaWithListStatusEntry : Any, MediaCompactWithTagsEntry, StudioEntry>(
    private val aniListApi: AuthedAniListApi,
    private val mediaListStatusController: MediaListStatusController,
    private val activityStatusController: ActivityStatusController,
    private val ignoreController: IgnoreController,
    private val settings: MediaDataSettings,
    private val markdown: Markdown,
    featureOverrideProvider: FeatureOverrideProvider,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaDetailsRoute: MediaDetailsRoute,
    @Assisted activityEntryProvider: ActivityEntryProvider<ActivityEntry, MediaCompactWithTagsEntry>,
    @Assisted mediaWithListStatusEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaWithListStatusEntry>,
    @Assisted mediaCompactWithTagsEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaCompactWithTagsEntry>,
    @Assisted studioEntryProvider: StudioEntryProvider<StudioListRowFragment, StudioEntry, MediaWithListStatusEntry>,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<UserDestinations.User>(navigationTypeMap)
    val userId = destination.userId

    private val refresh = RefreshFlow()

    // authedUser is observed first because it can update when the user logs in/out, which should
    // refresh the screen even if the user hasn't explicitly requested a refresh
    val entry = aniListApi.authedUser
        .flatMapLatest { viewer ->
            flowForRefreshableContent(refresh, Res.string.anime_user_error_loading) {
                flowFromSuspend {
                    val userOrViewerId = (userId ?: viewer?.id)!!
                    val user = aniListApi.user(userOrViewerId)!!
                    val about = user.about?.let(markdown::convertMarkdownText)
                    AniListUserScreen.Entry(user, about)
                }
            }
        }
        .foldPreviousResult()
        .stateIn(viewModelScope, SharingStarted.Eagerly, LoadingResult.loading())

    val viewer = aniListApi.authedUser

    val anime = MutableStateFlow(PagingData.empty<MediaWithListStatusEntry>())
    val manga = MutableStateFlow(PagingData.empty<MediaWithListStatusEntry>())
    val characters = MutableStateFlow(PagingData.empty<CharacterDetails>())
    val staff = MutableStateFlow(PagingData.empty<StaffDetails>())

    var studios = entry
        .map {
            withContext(CustomDispatchers.IO) {
                it.transformResult {
                    val result = it.user.favourites?.studios
                    val hasMore = result?.pageInfo?.hasNextPage == true
                    val studios = result?.nodes?.filterNotNull()?.map {
                        val media = (it.main?.nodes?.filterNotNull().orEmpty()
                                + it.nonMain?.nodes?.filterNotNull().orEmpty())
                            .distinctBy { it.id }
                            .map(mediaWithListStatusEntryProvider::mediaEntry)
                        studioEntryProvider.studioEntry(it, media)
                    }.orEmpty()
                    UserStudiosEntry(hasMore = hasMore, studios = studios)
                }
            }
        }
        .foldPreviousResult()
        .stateIn(viewModelScope, SharingStarted.Eagerly, LoadingResult.loading())

    val activities =
        entry.mapLatestNotNull { it.result }
            .flatMapLatest { entry ->
                combine(
                    activitySortFilterController.filterParams,
                    refresh.updates,
                    ::Pair
                ).flatMapLatest { (filterParams) ->
                    AniListPager {
                        val result = aniListApi.userSocialActivity(
                            isFollowing = null,
                            page = it,
                            userId = entry.user.id.toString(),
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
                    activityEntryProvider.activityEntry(it)
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
                            activityId = activityEntryProvider.id(it),
                            activityStatusAware = activityEntryProvider.activityStatusAware(it),
                            mediaFilterable = activityEntryProvider.mediaFilterable(it),
                            copyMedia = { mediaFilterable ->
                                activityEntryProvider.copyActivityEntry(
                                    this,
                                    activityEntryProvider.media(this)?.let {
                                        mediaCompactWithTagsEntryProvider.copyMediaEntry(
                                            it,
                                            mediaFilterable,
                                        )
                                    }
                                )
                            },
                            copyActivity = { liked, subscribed ->
                                activityEntryProvider.copyActivityEntry(
                                    entry = this,
                                    liked = liked,
                                    subscribed = subscribed,
                                )
                            }
                        )
                    }
                }
            }
//            .cachedIn(viewModelScope)
//            .flowOn(CustomDispatchers.IO)
//            .stateIn(viewModelScope, SharingStarted.Eagerly, PagingData.empty())

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

    @Composable
    fun isFollowing(): Boolean {
        val entry by entry.collectAsState()
        return (initialFollowState ?: toggleFollowingResult?.isFollowing
        ?: entry.result?.user?.isFollowing) == true
    }

    init {
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
            map = mediaWithListStatusEntryProvider::mediaEntry,
            id = mediaWithListStatusEntryProvider::id,
            property = anime,
            transformFlow = {
                applyMediaStatusChanges(
                    statusController = mediaListStatusController,
                    ignoreController = ignoreController,
                    mediaFilteringData = settings.mediaFilteringData(false),
                    mediaFilterable = mediaWithListStatusEntryProvider::mediaFilterable,
                    copy = { mediaWithListStatusEntryProvider.copyMediaEntry(this, it) },
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
            map = mediaWithListStatusEntryProvider::mediaEntry,
            id = mediaWithListStatusEntryProvider::id,
            property = manga,
            transformFlow = {
                applyMediaStatusChanges(
                    statusController = mediaListStatusController,
                    ignoreController = ignoreController,
                    mediaFilteringData = settings.mediaFilteringData(false),
                    mediaFilterable = mediaWithListStatusEntryProvider::mediaFilterable,
                    copy = { mediaWithListStatusEntryProvider.copyMediaEntry(this, it) },
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
    }

    private fun <ResponseType : Any, ResultType : Any> collectFavoritesPage(
        request: suspend (AniListUserScreen.Entry, page: Int) -> Pair<PaginationInfo?, List<ResponseType>>,
        map: (ResponseType) -> ResultType,
        id: (ResultType) -> String,
        property: MutableStateFlow<PagingData<ResultType>>,
        transformFlow: (Flow<PagingData<ResultType>>.() -> Flow<PagingData<ResultType>>)? = null,
    ) {
        viewModelScope.launch(CustomDispatchers.IO) {
            entry.mapLatestNotNull { it.result }
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
        initialFollowState = entry.value.result?.user?.isFollowing?.not() != false
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
        ) : UserStatsDetailScreen.State<Value> {
            val refreshRequest = MutableStateFlow("")
            val mediaFlows =
                mutableMapOf<String, Flow<Result<Map<Int, MediaTitlesAndImagesQuery.Data.Page.Medium>>>>()

            @Composable
            override fun getMedia(value: Value) = getMedia(value, this)
        }
    }

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val mediaListStatusController: MediaListStatusController,
        private val activityStatusController: ActivityStatusController,
        private val ignoreController: IgnoreController,
        private val settings: MediaDataSettings,
        private val markdown: Markdown,
        private val featureOverrideProvider: FeatureOverrideProvider,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: SavedStateHandle,
        @Assisted private val mediaDetailsRoute: MediaDetailsRoute,
    ) {
        fun <ActivityEntry : Any, MediaWithListStatusEntry : Any, MediaCompactWithTagsEntry, StudioEntry> create(
            activityEntryProvider: ActivityEntryProvider<ActivityEntry, MediaCompactWithTagsEntry>,
            mediaWithListStatusEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaWithListStatusEntry>,
            mediaCompactWithTagsEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaCompactWithTagsEntry>,
            studioEntryProvider: StudioEntryProvider<StudioListRowFragment, StudioEntry, MediaWithListStatusEntry>,
        ) = AniListUserViewModel(
            aniListApi = aniListApi,
            mediaListStatusController = mediaListStatusController,
            activityStatusController = activityStatusController,
            ignoreController = ignoreController,
            settings = settings,
            markdown = markdown,
            featureOverrideProvider = featureOverrideProvider,
            navigationTypeMap = navigationTypeMap,
            savedStateHandle = savedStateHandle,
            mediaDetailsRoute = mediaDetailsRoute,
            activityEntryProvider = activityEntryProvider,
            mediaWithListStatusEntryProvider = mediaWithListStatusEntryProvider,
            mediaCompactWithTagsEntryProvider = mediaCompactWithTagsEntryProvider,
            studioEntryProvider = studioEntryProvider,
        )
    }
}
