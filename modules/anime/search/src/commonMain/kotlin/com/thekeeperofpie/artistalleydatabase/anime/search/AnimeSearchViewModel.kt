package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.collection.LruCache
import androidx.lifecycle.SavedStateHandle
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
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.anilist.data.fragment.MediaWithListStatus
import com.anilist.data.fragment.StudioListRowFragment
import com.anilist.data.fragment.UserNavigationData
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.filter.CharacterSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.search.data.AnimeSearchMediaPagingSource
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.StaffEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.filter.StaffSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.StudioEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.filter.StudiosSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.users.data.UserEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.users.data.filter.UsersSortFilterParams
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.PagingUtils
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
class AnimeSearchViewModel<MediaPreviewEntry : Any, MediaWithListStatusEntry, CharacterEntry, StaffEntry, StudioEntry, UserEntry>(
    aniListApi: AuthedAniListApi,
    settings: MediaDataSettings,
    private val statusController: MediaListStatusController,
    val ignoreController: IgnoreController,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted val unlocked: StateFlow<Boolean>,
    @Assisted animeSortFilterParams: StateFlow<MediaSearchFilterParams<MediaSortOption>>,
    @Assisted private val animeFilterMedia: (
        result: PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>,
        transform: (AnimeSearchEntry.Media<MediaPreviewEntry>) -> MediaPreview,
    ) -> Flow<PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>>,
    @Assisted mangaSortFilterParams: StateFlow<MediaSearchFilterParams<MediaSortOption>>,
    @Assisted private val mangaFilterMedia: (
        result: PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>,
        transform: (AnimeSearchEntry.Media<MediaPreviewEntry>) -> MediaPreview,
    ) -> Flow<PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>>,
    @Assisted characterSortFilterParams: StateFlow<CharacterSortFilterParams>,
    @Assisted staffSortFilterParams: StateFlow<StaffSortFilterParams>,
    @Assisted studiosSortFilterParams: StateFlow<StudiosSortFilterParams>,
    @Assisted usersSortFilterParams: StateFlow<UsersSortFilterParams>,
    @Assisted mediaPreviewWithDescriptionEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaPreviewEntry>,
    @Assisted mediaWithListStatusEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaWithListStatusEntry>,
    @Assisted characterEntryProvider: CharacterEntryProvider<CharacterAdvancedSearchQuery.Data.Page.Character, CharacterEntry, MediaWithListStatusEntry>,
    @Assisted staffEntryProvider: StaffEntryProvider<StaffSearchQuery.Data.Page.Staff, StaffEntry, MediaWithListStatusEntry>,
    @Assisted studioEntryProvider: StudioEntryProvider<StudioListRowFragment, StudioEntry, MediaWithListStatusEntry>,
    @Assisted userEntryProvider: UserEntryProvider<UserNavigationData, UserEntry, MediaWithListStatusEntry>,
) : ViewModel() {

    private val destination = if (savedStateHandle.keys().isEmpty()) {
        SearchDestinations.SearchMedia(
            sort = MediaSortOption.SEARCH_MATCH,
            lockSortOverride = false,
        )
    } else {
        savedStateHandle.toDestination<SearchDestinations.SearchMedia>(navigationTypeMap)
    }

    val mediaViewOption = savedStateHandle.getMutableStateFlow(
        key = "mediaViewOption",
        initialValue = { settings.mediaViewOption.value },
        deserialize = MediaViewOption::valueOf,
        serialize = MediaViewOption::name,
    )
    val viewer = aniListApi.authedUser
    val query = savedStateHandle.getMutableStateFlow<String>("query") { "" }
    var content = MutableStateFlow(PagingUtils.loading<AnimeSearchEntry>())

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
                        -> PagingData.Companion.empty<AnimeSearchEntry.Media<MediaPreviewEntry>>(loadStates)
                    SearchType.CHARACTER -> PagingData.Companion.empty<AnimeSearchEntry.Character<CharacterEntry>>(
                        loadStates
                    )
                    SearchType.STAFF -> PagingData.Companion.empty<AnimeSearchEntry.Staff<StaffEntry>>(
                        loadStates
                    )
                    SearchType.STUDIO -> PagingData.Companion.empty<AnimeSearchEntry.Studio<StudioEntry>>(
                        loadStates
                    )
                    SearchType.USER -> PagingData.Companion.empty<AnimeSearchEntry.User<UserEntry>>(loadStates)
                }.let(::MutableStateFlow)
            }.associate { it }
        }

    // TODO: lockSort was removed, was it ever used?
    init {
        viewModelScope.launch(CustomDispatchers.Companion.Main) {
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
                    animeSortFilterParams,
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
                animeFilterMedia(it) {
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
                    mangaSortFilterParams,
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
                mangaFilterMedia(it) {
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
                    it.id.toString(),
                    characterEntryProvider.characterEntry(
                        it,
                        it.media?.edges?.mapNotNull { it?.node }.orEmpty()
                            .distinctBy { it.id }
                            .map(mediaWithListStatusEntryProvider::mediaEntry)
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
                                entry = characterEntryProvider.copyCharacterEntry(
                                    entry = it.entry,
                                    media = characterEntryProvider.media(it.entry).mapNotNull {
                                        applyMediaFiltering(
                                            statuses = statuses,
                                            ignoreController = ignoreController,
                                            filteringData = filteringData,
                                            entry = it,
                                            filterableData =
                                                mediaWithListStatusEntryProvider.mediaFilterable(it),
                                            copy = {
                                                mediaWithListStatusEntryProvider
                                                    .copyMediaEntry(this, it)
                                            },
                                        )
                                    },
                                )
                            )
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
                    staffSortFilterParams,
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
                        sort = filterParams.sort.toApiValueForSearch(filterParams.sortAscending),
                        isBirthday = filterParams.isBirthday,
                    ).page.run { pageInfo to staff }
                }
            },
            id = { it.id },
            entry = {
                AnimeSearchEntry.Staff(
                    it.id.toString(),
                    staffEntryProvider.staffEntry(
                        it,
                        it.staffMedia?.nodes?.filterNotNull().orEmpty()
                            .distinctBy { it.id }
                            .map(mediaWithListStatusEntryProvider::mediaEntry)
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
                                entry = staffEntryProvider.copyStaffEntry(
                                    entry = it.entry,
                                    media = staffEntryProvider.media(it.entry).mapNotNull {
                                        applyMediaFiltering(
                                            statuses = statuses,
                                            ignoreController = ignoreController,
                                            filteringData = filteringData,
                                            entry = it,
                                            filterableData =
                                                mediaWithListStatusEntryProvider.mediaFilterable(it),
                                            copy = {
                                                mediaWithListStatusEntryProvider
                                                    .copyMediaEntry(this, it)
                                            },
                                        )
                                    },
                                )
                            )
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
                    studiosSortFilterParams,
                    ::Triple,
                )
            },
            pagingSource = { (query, _, filterParams), cache: LruCache<Int, PagingSource.LoadResult.Page<Int, StudioSearchQuery.Data.Page.Studio>> ->
                AniListPagingSource(cache = cache, perPage = 25) {
                    aniListApi.searchStudios(
                        query = query,
                        page = it,
                        perPage = 25,
                        sort = filterParams.sort.toApiValue(filterParams.sortAscending),
                    ).page.run { pageInfo to studios }
                }
            },
            id = { it.id },
            entry = {
                AnimeSearchEntry.Studio(
                    it.id.toString(),
                    studioEntryProvider.studioEntry(
                        it, (it.main?.nodes?.filterNotNull().orEmpty()
                                + it.nonMain?.nodes?.filterNotNull().orEmpty())
                            .distinctBy { it.id }
                            .map(mediaWithListStatusEntryProvider::mediaEntry)
                    ),
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
                                entry = studioEntryProvider.copyStudioEntry(
                                    it.entry,
                                    studioEntryProvider.media(it.entry).mapNotNull {
                                        applyMediaFiltering(
                                            statuses = statuses,
                                            ignoreController = ignoreController,
                                            filteringData = filteringData,
                                            entry = it,
                                            filterableData =
                                                mediaWithListStatusEntryProvider.mediaFilterable(it),
                                            copy = {
                                                mediaWithListStatusEntryProvider
                                                    .copyMediaEntry(this, it)
                                            },
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
                    usersSortFilterParams,
                    refresh.updates,
                    ::Triple,
                )
            },
            pagingSource = { (query, filterParams), cache: LruCache<Int, PagingSource.LoadResult.Page<Int, UserSearchQuery.Data.Page.User>> ->
                AniListPagingSource {
                    aniListApi.searchUsers(
                        query = query,
                        page = it,
                        sort = filterParams.sort.toApiValue(filterParams.sortAscending),
                        isModerator = filterParams.isModerator,
                    ).page.run { pageInfo to users }
                }
            },
            id = { it.id },
            entry = {
                val anime = it.favourites?.anime?.edges
                    ?.filterNotNull()
                    ?.sortedBy { it.favouriteOrder }
                    ?.mapNotNull { it.node }
                    .orEmpty()
                    .map(mediaWithListStatusEntryProvider::mediaEntry)

                val manga = it.favourites?.manga?.edges
                    ?.filterNotNull()
                    ?.sortedBy { it.favouriteOrder }
                    ?.mapNotNull { it.node }
                    .orEmpty()
                    .map(mediaWithListStatusEntryProvider::mediaEntry)
                AnimeSearchEntry.User(
                    it.id.toString(),
                    userEntryProvider.userEntry(
                        it,
                        (anime + manga).distinctBy(mediaWithListStatusEntryProvider::id)
                    ),
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
                                entry = userEntryProvider.copyUserEntry(
                                    entry = it.entry,
                                    media = userEntryProvider.media(it.entry).mapNotNull {
                                        applyMediaFiltering(
                                            statuses = statuses,
                                            ignoreController = ignoreController,
                                            filteringData = filteringData,
                                            entry = it,
                                            filterableData =
                                                mediaWithListStatusEntryProvider.mediaFilterable(it),
                                            copy = {
                                                mediaWithListStatusEntryProvider
                                                    .copyMediaEntry(this, it)
                                            },
                                        )
                                    }
                                ),
                            )
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
        viewModelScope.launch(CustomDispatchers.Companion.Main) {
            unlocked.filter { it || searchType == SearchType.ANIME || searchType == SearchType.MANGA }
                .flatMapLatest { selectedType }
                .filter { it == searchType }
                .flatMapLatest { flow() }
                .flowOn(CustomDispatchers.Companion.IO)
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
        private val settings: MediaDataSettings,
        private val statusController: MediaListStatusController,
        private val ignoreController: IgnoreController,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: SavedStateHandle,
        @Assisted private val unlocked: StateFlow<Boolean>,
        @Assisted private val animeSortFilterParams: StateFlow<MediaSearchFilterParams<MediaSortOption>>,
        @Assisted private val mangaSortFilterParams: StateFlow<MediaSearchFilterParams<MediaSortOption>>,
        @Assisted private val characterSortFilterParams: StateFlow<CharacterSortFilterParams>,
        @Assisted private val staffSortFilterParams: StateFlow<StaffSortFilterParams>,
        @Assisted private val studiosSortFilterParams: StateFlow<StudiosSortFilterParams>,
        @Assisted private val usersSortFilterParams: StateFlow<UsersSortFilterParams>,
    ) {
        fun <MediaPreviewEntry : Any, MediaWithListStatusEntry, CharacterEntry, StaffEntry, StudioEntry, UserEntry> create(
            animeFilterMedia: (
                result: PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>,
                transform: (AnimeSearchEntry.Media<MediaPreviewEntry>) -> MediaPreview,
            ) -> Flow<PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>>,
            mangaFilterMedia: (
                result: PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>,
                transform: (AnimeSearchEntry.Media<MediaPreviewEntry>) -> MediaPreview,
            ) -> Flow<PagingData<AnimeSearchEntry.Media<MediaPreviewEntry>>>,
            mediaPreviewWithDescriptionEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaPreviewEntry>,
            mediaWithListStatusEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaWithListStatusEntry>,
            characterEntryProvider: CharacterEntryProvider<CharacterAdvancedSearchQuery.Data.Page.Character, CharacterEntry, MediaWithListStatusEntry>,
            staffEntryProvider: StaffEntryProvider<StaffSearchQuery.Data.Page.Staff, StaffEntry, MediaWithListStatusEntry>,
            studioEntryProvider: StudioEntryProvider<StudioListRowFragment, StudioEntry, MediaWithListStatusEntry>,
            userEntryProvider: UserEntryProvider<UserNavigationData, UserEntry, MediaWithListStatusEntry>,
        ) = AnimeSearchViewModel(
            aniListApi = aniListApi,
            settings = settings,
            statusController = statusController,
            ignoreController = ignoreController,
            unlocked = unlocked,
            navigationTypeMap = navigationTypeMap,
            animeSortFilterParams = animeSortFilterParams,
            animeFilterMedia = animeFilterMedia,
            mangaSortFilterParams = mangaSortFilterParams,
            mangaFilterMedia = mangaFilterMedia,
            characterSortFilterParams = characterSortFilterParams,
            staffSortFilterParams = staffSortFilterParams,
            studiosSortFilterParams = studiosSortFilterParams,
            usersSortFilterParams = usersSortFilterParams,
            savedStateHandle = savedStateHandle,
            mediaPreviewWithDescriptionEntryProvider = mediaPreviewWithDescriptionEntryProvider,
            mediaWithListStatusEntryProvider = mediaWithListStatusEntryProvider,
            characterEntryProvider = characterEntryProvider,
            staffEntryProvider = staffEntryProvider,
            studioEntryProvider = studioEntryProvider,
            userEntryProvider = userEntryProvider,
        )
    }
}
