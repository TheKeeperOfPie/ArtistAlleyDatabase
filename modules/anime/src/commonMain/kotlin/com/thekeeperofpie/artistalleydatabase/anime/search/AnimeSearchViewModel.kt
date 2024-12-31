package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.collection.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.anilist.data.CharacterAdvancedSearchQuery
import com.anilist.data.MediaAdvancedSearchQuery
import com.anilist.data.StaffSearchQuery
import com.anilist.data.StudioSearchQuery
import com.anilist.data.UserSearchQuery
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.anilist.data.type.MediaType
import com.anilist.data.type.StudioSort
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.search.data.AnimeSearchMediaPagingSource
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSortOption
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioListRowFragmentEntry
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.users.UserListRow
import com.thekeeperofpie.artistalleydatabase.anime.users.UserSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.users.UserSortOption
import com.thekeeperofpie.artistalleydatabase.anime.users.UserUtils
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.ScopedSavedStateHandle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Inject
class AnimeSearchViewModel<MediaEntry>(
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    private val statusController: MediaListStatusController,
    val ignoreController: IgnoreController,
    featureOverrideProvider: FeatureOverrideProvider,
    private val monetizationController: MonetizationController,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: ScopedSavedStateHandle,
    @Assisted animeSortFilterViewModel: MediaSortFilterViewModel<MediaSortOption>,
    @Assisted mangaSortFilterViewModel: MediaSortFilterViewModel<MediaSortOption>,
    @Assisted characterSortFilterParams: StateFlow<CharacterSortFilterParams>,
    @Assisted mediaPreviewWithDescriptionEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaEntry>,
) : ViewModel() {

    private val destination =
        if (savedStateHandle.savedStateHandle.keys().isEmpty()) {
            AnimeDestination.SearchMedia(
                sort = MediaSortOption.SEARCH_MATCH,
                lockSortOverride = false,
            )
        } else {
            savedStateHandle.savedStateHandle
                .toDestination<AnimeDestination.SearchMedia>(navigationTypeMap)
        }

    val mediaViewOption = savedStateHandle.getMutableStateFlow(
        key = "mediaViewOption",
        initialValue = { settings.mediaViewOption.value },
        deserialize = MediaViewOption::valueOf,
        serialize = MediaViewOption::name,
    )
    val viewer = aniListApi.authedUser
    val query = savedStateHandle.getMutableStateFlow<String>("query") { "" }
    var content = MutableStateFlow(PagingData.empty<AnimeSearchEntry>())

    val unlocked = monetizationController.unlocked

    val staffSortFilterController =
        StaffSortFilterController(viewModelScope, settings, featureOverrideProvider)

    val studioSortFilterController =
        StudioSortFilterController(viewModelScope, settings, featureOverrideProvider)

    val userSortFilterController =
        UserSortFilterController(viewModelScope, settings, featureOverrideProvider)

    private val refresh = RefreshFlow()

    val selectedType = MutableStateFlow(
        destination.mediaType?.let {
            when (it) {
                MediaType.ANIME,
                MediaType.UNKNOWN__,
                    -> SearchType.ANIME
                MediaType.MANGA -> SearchType.MANGA
            }
        } ?: run {
            if (settings.preferredMediaType.value == MediaType.ANIME) {
                SearchType.ANIME
            } else {
                SearchType.MANGA
            }
        }
    )
    private val results =
        LoadStates(
            LoadState.Loading,
            LoadState.NotLoading(true),
            LoadState.NotLoading(true),
        ).let { loadStates ->
            SearchType.entries.map {
                it to when (it) {
                    SearchType.ANIME,
                    SearchType.MANGA,
                        -> PagingData.empty<AnimeSearchEntry.Media<MediaEntry>>(loadStates)
                    SearchType.CHARACTER -> PagingData.empty<AnimeSearchEntry.Character>(loadStates)
                    SearchType.STAFF -> PagingData.empty<AnimeSearchEntry.Staff>(loadStates)
                    SearchType.STUDIO -> PagingData.empty<AnimeSearchEntry.Studio>(loadStates)
                    SearchType.USER -> PagingData.empty<AnimeSearchEntry.User>(loadStates)
                }.let(::MutableStateFlow)
            }.associate { it }
        }

    // TODO: lockSort was removed, was it ever used?
    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            selectedType
                .flatMapLatest { results[it]!! }
                .collectLatest {
                    @Suppress("UNCHECKED_CAST")
                    content.emit(it as PagingData<AnimeSearchEntry>)
                }
        }

        val includeDescriptionFlow =
            MediaDataUtils.mediaViewOptionIncludeDescriptionFlow(mediaViewOption)

        collectSearch(
            searchType = SearchType.ANIME,
            flow = {
                combine(
                    query.debounce(1.seconds),
                    includeDescriptionFlow,
                    refresh.updates,
                    animeSortFilterViewModel.filterParams,
                    settings.showAdult,
                    AnimeSearchMediaPagingSource::RefreshParams
                )
            },
            pagingSource = { params, cache: LruCache<Int, PagingSource.LoadResult.Page<Int, MediaAdvancedSearchQuery.Data.Page.Medium>> ->
                AnimeSearchMediaPagingSource(
                    aniListApi = aniListApi,
                    refreshParams = params,
                    cache = cache,
                    mediaType = MediaType.ANIME,
                )
            },
            id = { it.id },
            entry = {
                AnimeSearchEntry.Media(
                    it.id.toString(),
                    mediaPreviewWithDescriptionEntryProvider.mediaEntry(it)
                )
            },
            filter = {
                animeSortFilterViewModel.filterMedia(it) {
                    mediaPreviewWithDescriptionEntryProvider.media(it.entry)
                }
            },
            finalTransform = {
                applyMediaStatusChanges(
                    statusController = statusController,
                    ignoreController = ignoreController,
                    mediaFilteringData = settings.mediaFilteringData(false),
                    mediaFilterable = { mediaPreviewWithDescriptionEntryProvider.mediaFilterable(it.entry) },
                    copy = {
                        copy(
                            entry = mediaPreviewWithDescriptionEntryProvider.copyMediaEntry(
                                entry,
                                it
                            )
                        )
                    },
                )
            }
        )

        collectSearch(
            searchType = SearchType.MANGA,
            flow = {
                combine(
                    query.debounce(1.seconds),
                    includeDescriptionFlow,
                    refresh.updates,
                    mangaSortFilterViewModel.filterParams,
                    settings.showAdult,
                    AnimeSearchMediaPagingSource::RefreshParams
                )
            },
            pagingSource = { params, cache: LruCache<Int, PagingSource.LoadResult.Page<Int, MediaAdvancedSearchQuery.Data.Page.Medium>> ->
                AnimeSearchMediaPagingSource(
                    aniListApi = aniListApi,
                    refreshParams = params,
                    cache = cache,
                    mediaType = MediaType.MANGA,
                )
            },
            id = { it.id },
            entry = {
                AnimeSearchEntry.Media(
                    it.id.toString(),
                    mediaPreviewWithDescriptionEntryProvider.mediaEntry(it)
                )
            },
            filter = {
                mangaSortFilterViewModel.filterMedia(it) {
                    mediaPreviewWithDescriptionEntryProvider.media(it.entry)
                }
            },
            finalTransform = {
                applyMediaStatusChanges(
                    statusController = statusController,
                    ignoreController = ignoreController,
                    mediaFilteringData = settings.mediaFilteringData(false),
                    mediaFilterable = { mediaPreviewWithDescriptionEntryProvider.mediaFilterable(it.entry) },
                    copy = {
                        copy(
                            entry = mediaPreviewWithDescriptionEntryProvider.copyMediaEntry(
                                entry,
                                it
                            )
                        )
                    },
                )
            }
        )

        collectSearch(
            searchType = SearchType.CHARACTER,
            flow = {
                combine(
                    query.debounce(1.seconds),
                    characterSortFilterParams,
                    refresh.updates,
                    ::Triple
                )
            },
            pagingSource = { (query, filterParams), cache: LruCache<Int, PagingSource.LoadResult.Page<Int, CharacterAdvancedSearchQuery.Data.Page.Character>> ->
                AniListPagingSource(cache = cache, perPage = 25) {
                    aniListApi.searchCharacters(
                        query = query,
                        page = it,
                        perPage = 25,
                        sort = filterParams.sort.toApiValueForSearch(filterParams.sortAscending),
                        isBirthday = filterParams.isBirthday,
                    ).page.run { pageInfo to characters }
                }
            },
            id = { it.id },
            entry = {
                AnimeSearchEntry.Character(
                    CharacterListRow.Entry(
                        character = it,
                        media = it.media?.edges?.mapNotNull { it?.node }.orEmpty()
                            .distinctBy { it.id }
                            .map(::MediaWithListStatusEntry)
                    )
                )
            },
            finalTransform = {
                flatMapLatest {
                    combine(
                        statusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { statuses, _, filteringData ->
                        it.mapNotNull {
                            it.copy(entry = it.entry.copy(media = it.entry.media.mapNotNull {
                                applyMediaFiltering(
                                    statuses = statuses,
                                    ignoreController = ignoreController,
                                    filteringData = filteringData,
                                    entry = it,
                                    filterableData = it.mediaFilterable,
                                    copy = { copy(mediaFilterable = it) },
                                )
                            }))
                        }
                    }
                }
            }
        )

        collectSearch(
            searchType = SearchType.STAFF,
            flow = {
                combine(
                    query.debounce(1.seconds),
                    staffSortFilterController.filterParams,
                    refresh.updates,
                    ::Triple
                )
            },
            pagingSource = { (query, filterParams), cache: LruCache<Int, PagingSource.LoadResult.Page<Int, StaffSearchQuery.Data.Page.Staff>> ->
                AniListPagingSource(cache = cache, perPage = 25) {
                    aniListApi.searchStaff(
                        query = query,
                        page = it,
                        perPage = 25,
                        sort = filterParams.sort.selectedOption(StaffSortOption.SEARCH_MATCH)
                            .toApiValueForSearch(filterParams.sortAscending),
                        isBirthday = filterParams.isBirthday,
                    ).page.run { pageInfo to staff }
                }
            },
            id = { it.id },
            entry = {
                AnimeSearchEntry.Staff(
                    StaffListRow.Entry(
                        staff = it,
                        media = it.staffMedia?.nodes?.filterNotNull().orEmpty()
                            .distinctBy { it.id }
                            .map(::MediaWithListStatusEntry)
                    )
                )
            },
            finalTransform = {
                flatMapLatest {
                    combine(
                        statusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { statuses, _, filteringData ->
                        it.mapNotNull {
                            it.copy(entry = it.entry.copy(media = it.entry.media.mapNotNull {
                                applyMediaFiltering(
                                    statuses = statuses,
                                    ignoreController = ignoreController,
                                    filteringData = filteringData,
                                    entry = it,
                                    filterableData = it.mediaFilterable,
                                    copy = { copy(mediaFilterable = it) },
                                )
                            }))
                        }
                    }
                }
            }
        )

        collectSearch(
            searchType = SearchType.STUDIO,
            flow = {
                combine(
                    query.debounce(1.seconds),
                    refresh.updates,
                    studioSortFilterController.filterParams,
                    ::Triple,
                )
            },
            pagingSource = { (query, _, filterParams), cache: LruCache<Int, PagingSource.LoadResult.Page<Int, StudioSearchQuery.Data.Page.Studio>> ->
                AniListPagingSource(cache = cache, perPage = 25) {
                    aniListApi.searchStudios(
                        query = query,
                        page = it,
                        perPage = 25,
                        sort = filterParams.sort.filter { it.state == FilterIncludeExcludeState.INCLUDE }
                            .flatMap { it.value.toApiValue(filterParams.sortAscending) }
                            .ifEmpty { listOf(StudioSort.SEARCH_MATCH) }
                    ).page.run { pageInfo to studios }
                }
            },
            id = { it.id },
            entry = {
                AnimeSearchEntry.Studio(
                    StudioListRowFragmentEntry(
                        studio = it,
                        media = (it.main?.nodes?.filterNotNull().orEmpty()
                                + it.nonMain?.nodes?.filterNotNull().orEmpty())
                            .distinctBy { it.id }
                            .map(::MediaWithListStatusEntry),
                    )
                )
            },
            finalTransform = {
                flatMapLatest {
                    combine(
                        statusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { statuses, _, filteringData ->
                        it.mapNotNull {
                            it.copy(
                                entry =
                                    StudioListRowFragmentEntry.provider<MediaWithListStatusEntry>()
                                        .copyStudioEntry(
                                            it.entry,
                                            it.entry.media.mapNotNull {
                                                applyMediaFiltering(
                                                    statuses = statuses,
                                                    ignoreController = ignoreController,
                                                    filteringData = filteringData,
                                                    entry = it,
                                                    filterableData = it.mediaFilterable,
                                                    copy = { copy(mediaFilterable = it) },
                                                )
                                            }
                                        )
                            )
                        }
                    }
                }
            }
        )

        collectSearch(
            searchType = SearchType.USER,
            flow = {
                combine(
                    query.debounce(1.seconds),
                    userSortFilterController.filterParams,
                    refresh.updates,
                    ::Triple,
                )
            },
            pagingSource = { (query, filterParams), cache: LruCache<Int, PagingSource.LoadResult.Page<Int, UserSearchQuery.Data.Page.User>> ->
                AniListPagingSource {
                    aniListApi.searchUsers(
                        query = query,
                        page = it,
                        sort = filterParams.sort.selectedOption(UserSortOption.SEARCH_MATCH)
                            .toApiValue(filterParams.sortAscending),
                        isModerator = filterParams.isModerator,
                    ).page.run { pageInfo to users }
                }
            },
            id = { it.id },
            entry = {
                AnimeSearchEntry.User(
                    UserListRow.Entry(
                        user = it,
                        media = UserUtils.buildInitialMediaEntries(
                            user = it,
                            mediaEntryProvider = MediaWithListStatusEntry.Provider,
                        ),
                    )
                )
            },
            finalTransform = {
                flatMapLatest {
                    combine(
                        statusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { statuses, _, filteringData ->
                        it.mapNotNull {
                            it.copy(entry = it.entry.copy(media = it.entry.media.mapNotNull {
                                applyMediaFiltering(
                                    statuses = statuses,
                                    ignoreController = ignoreController,
                                    filteringData = filteringData,
                                    entry = it,
                                    filterableData = it.mediaFilterable,
                                    copy = { copy(mediaFilterable = it) },
                                )
                            }))
                        }
                    }
                }
            }
        )
    }

    private fun <Params, Result : Any, Entry : AnimeSearchEntry> collectSearch(
        searchType: SearchType,
        flow: () -> Flow<Params>,
        pagingSource: (Params, LruCache<Int, PagingSource.LoadResult.Page<Int, Result>>) -> PagingSource<Int, Result>,
        id: (Result) -> Int,
        entry: (Result) -> Entry,
        filter: ((PagingData<Entry>) -> Flow<PagingData<Entry>>)? = null,
        finalTransform: Flow<PagingData<Entry>>.() -> Flow<PagingData<Entry>> = { this },
    ) {
        viewModelScope.launch(CustomDispatchers.Main) {
            monetizationController.unlocked
                .filter { it || searchType == SearchType.ANIME || searchType == SearchType.MANGA }
                .flatMapLatest { selectedType }
                .filter { it == searchType }
                .flatMapLatest { flow() }
                .flowOn(CustomDispatchers.IO)
                .debounce(100.milliseconds)
                .distinctUntilChanged()
                .flatMapLatest {
                    val cache = LruCache<Int, PagingSource.LoadResult.Page<Int, Result>>(20)
                    Pager(
                        PagingConfig(
                            pageSize = 25,
                            initialLoadSize = 25,
                            jumpThreshold = 25,
                            enablePlaceholders = true,
                        )
                    ) {
                        pagingSource(it, cache)
                    }.flow
                }
                .map {
                    // AniList can return duplicates across pages, manually enforce uniqueness
                    val seenIds = mutableSetOf<Int>()
                    it.filterOnIO { seenIds.add(id(it)) }.mapOnIO { entry(it) }
                }
                .cachedIn(viewModelScope)
                .run {
                    if (filter != null) flatMapLatest { filter(it) } else this
                }
                .run(finalTransform)
                .cachedIn(viewModelScope)
                .collectLatest {
                    results[searchType]!!.emit(it)
                }
        }
    }

    fun onRefresh() = refresh.refresh()

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val settings: AnimeSettings,
        private val statusController: MediaListStatusController,
        private val ignoreController: IgnoreController,
        private val featureOverrideProvider: FeatureOverrideProvider,
        private val monetizationController: MonetizationController,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: ScopedSavedStateHandle,
        @Assisted private val animeSortFilterViewModel: MediaSortFilterViewModel<MediaSortOption>,
        @Assisted private val mangaSortFilterViewModel: MediaSortFilterViewModel<MediaSortOption>,
        @Assisted private val characterSortFilterParams: StateFlow<CharacterSortFilterParams>,
    ) {
        fun <MediaEntry> create(
            mediaEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaEntry>,
        ) = AnimeSearchViewModel(
            aniListApi = aniListApi,
            settings = settings,
            statusController = statusController,
            ignoreController = ignoreController,
            featureOverrideProvider = featureOverrideProvider,
            monetizationController = monetizationController,
            navigationTypeMap = navigationTypeMap,
            animeSortFilterViewModel = animeSortFilterViewModel,
            mangaSortFilterViewModel = mangaSortFilterViewModel,
            characterSortFilterParams = characterSortFilterParams,
            savedStateHandle = savedStateHandle,
            mediaPreviewWithDescriptionEntryProvider = mediaEntryProvider,
        )
    }
}
