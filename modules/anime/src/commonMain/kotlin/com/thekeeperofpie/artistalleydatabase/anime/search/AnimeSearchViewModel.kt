package com.thekeeperofpie.artistalleydatabase.anime.search

import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_current_season
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_last_season
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_next_season
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_suggestions_recent_finished
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_anime
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_character
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_manga
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_staff
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_studio
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_user
import com.anilist.data.CharacterAdvancedSearchQuery
import com.anilist.data.MediaAdvancedSearchQuery
import com.anilist.data.StaffSearchQuery
import com.anilist.data.StudioSearchQuery
import com.anilist.data.UserSearchQuery
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaStatus
import com.anilist.data.type.MediaType
import com.anilist.data.type.StudioSort
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AiringDate
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MangaSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaGenresController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSortOption
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.user.UserListRow
import com.thekeeperofpie.artistalleydatabase.anime.user.UserSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.user.UserSortOption
import com.thekeeperofpie.artistalleydatabase.anime.user.UserUtils
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Inject
class AnimeSearchViewModel(
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    private val statusController: MediaListStatusController,
    val ignoreController: IgnoreController,
    mediaTagsController: MediaTagsController,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    featureOverrideProvider: FeatureOverrideProvider,
    private val monetizationController: MonetizationController,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    private val destination = if (savedStateHandle.keys().isEmpty()) {
        AnimeDestination.SearchMedia(
            sort = MediaSortOption.SEARCH_MATCH,
            lockSortOverride = false,
        )
    } else {
        savedStateHandle.toDestination<AnimeDestination.SearchMedia>(navigationTypeMap)
    }

    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)
    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    var content = MutableStateFlow(PagingData.empty<AnimeSearchEntry>())

    val unlocked = monetizationController.unlocked

    private val tagId = savedStateHandle.get<String?>("tagId")

    val animeSortFilterController = AnimeSearchSortFilterController(
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController,
        mediaGenresController = mediaGenresController,
        mediaLicensorsController = mediaLicensorsController,
    )

    class AnimeSearchSortFilterController(
        scope: CoroutineScope,
        aniListApi: AuthedAniListApi,
        settings: AnimeSettings,
        featureOverrideProvider: FeatureOverrideProvider,
        mediaTagsController: MediaTagsController,
        mediaGenresController: MediaGenresController,
        mediaLicensorsController: MediaLicensorsController,
    ) : AnimeSortFilterController<MediaSortOption>(
        sortTypeEnumClass = MediaSortOption::class,
        scope = scope,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController,
        mediaGenresController = mediaGenresController,
        mediaLicensorsController = mediaLicensorsController,
    ) {

        override val suggestionsSection = SortFilterSection.Suggestions(
            titleRes = Res.string.anime_media_filter_suggestions_label,
            titleDropdownContentDescriptionRes = Res.string.anime_media_filter_suggestions_expand_content_description,
            suggestions = AnimeFilterSuggestion.entries.toList(),
            onSuggestionClick = {
                when (it) {
                    AnimeFilterSuggestion.RECENT_FINISHED -> {
                        sortSection.changeSelected(
                            selected = MediaSortOption.END_DATE,
                            sortAscending = false,
                        )
                        statusSection.setIncluded(MediaStatus.FINISHED, locked = false)
                        formatSection.setIncluded(MediaFormat.TV, locked = false)
                        myListStatusSection.setExcluded(null, locked = false)
                        airingDate = AiringDate.Basic() to airingDate.second
                        airingDateIsAdvanced = false
                    }
                    AnimeFilterSuggestion.LAST_SEASON -> {
                        sortSection.changeSelected(
                            selected = MediaSortOption.POPULARITY,
                            sortAscending = false,
                        )
                        statusSection.clear()
                        formatSection.setIncluded(MediaFormat.TV, locked = false)
                        val (_, year) = AniListUtils.getPreviousSeasonYear()
                        airingDate = AiringDate.Basic(
                            AiringDate.SeasonOption.PREVIOUS,
                            year.toString(),
                        ) to airingDate.second
                        airingDateIsAdvanced = false
                        myListStatusSection.clear()
                    }
                    AnimeFilterSuggestion.CURRENT_SEASON -> {
                        sortSection.changeSelected(
                            selected = MediaSortOption.POPULARITY,
                            sortAscending = false,
                        )
                        statusSection.clear()
                        formatSection.setIncluded(MediaFormat.TV, locked = false)
                        val (_, year) = AniListUtils.getCurrentSeasonYear()
                        airingDate = AiringDate.Basic(
                            AiringDate.SeasonOption.CURRENT,
                            year.toString(),
                        ) to airingDate.second
                        airingDateIsAdvanced = false
                        myListStatusSection.clear()
                    }
                    AnimeFilterSuggestion.NEXT_SEASON -> {
                        sortSection.changeSelected(
                            selected = MediaSortOption.POPULARITY,
                            sortAscending = false,
                        )
                        statusSection.clear()
                        formatSection.setIncluded(MediaFormat.TV, locked = false)
                        val (_, year) = AniListUtils.getNextSeasonYear()
                        airingDate = AiringDate.Basic(
                            AiringDate.SeasonOption.NEXT,
                            year.toString(),
                        ) to airingDate.second
                        airingDateIsAdvanced = false
                        myListStatusSection.clear()
                    }
                }
            }
        )

        init {
            sortSection.setOptions(
                MediaSortOption.entries
                    .filter { it != MediaSortOption.VOLUMES }
                    .filter { it != MediaSortOption.CHAPTERS }
            )
        }

        enum class AnimeFilterSuggestion(private val textRes: StringResource) :
            SortFilterSection.Suggestions.Suggestion {
            RECENT_FINISHED(Res.string.anime_media_filter_suggestions_recent_finished),
            LAST_SEASON(Res.string.anime_media_filter_suggestions_last_season),
            CURRENT_SEASON(Res.string.anime_media_filter_suggestions_current_season),
            NEXT_SEASON(Res.string.anime_media_filter_suggestions_next_season),
            ;

            @Composable
            override fun text() = stringResource(textRes)
        }
    }

    val mangaSortFilterController = MangaSortFilterController(
        sortTypeEnumClass = MediaSortOption::class,
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController,
        mediaGenresController = mediaGenresController,
        mediaLicensorsController = mediaLicensorsController,
    ).apply {
        sortSection.setOptions(MediaSortOption.entries.filter { it != MediaSortOption.EPISODES })
    }

    val characterSortFilterController =
        CharacterSortFilterController(viewModelScope, settings, featureOverrideProvider)

    val staffSortFilterController =
        StaffSortFilterController(viewModelScope, settings, featureOverrideProvider)

    val studioSortFilterController =
        StudioSortFilterController(viewModelScope, settings, featureOverrideProvider)

    val userSortFilterController =
        UserSortFilterController(viewModelScope, settings, featureOverrideProvider)

    private val refresh = RefreshFlow()

    var selectedType by mutableStateOf(
        destination.mediaType?.let {
            when (it) {
                MediaType.ANIME,
                MediaType.UNKNOWN__,
                    -> AnimeSearchViewModel.SearchType.ANIME
                MediaType.MANGA -> AnimeSearchViewModel.SearchType.MANGA
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
                        -> PagingData.empty<AnimeSearchEntry.Media>(loadStates)
                    SearchType.CHARACTER -> PagingData.empty<AnimeSearchEntry.Character>(loadStates)
                    SearchType.STAFF -> PagingData.empty<AnimeSearchEntry.Staff>(loadStates)
                    SearchType.STUDIO -> PagingData.empty<AnimeSearchEntry.Studio>(loadStates)
                    SearchType.USER -> PagingData.empty<AnimeSearchEntry.User>(loadStates)
                }.let(::MutableStateFlow)
            }.associate { it }
        }

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { selectedType }
                .flatMapLatest { results[it]!! }
                .collectLatest {
                    @Suppress("UNCHECKED_CAST")
                    content.emit(it as PagingData<AnimeSearchEntry>)
                }
        }

        // TODO: Move lockSort into destination args?
        val lockSort =
            destination.lockSortOverride ?: (destination.tagId == null && destination.genre == null)
        animeSortFilterController.initialize(
            initialParams = AnimeSortFilterController.InitialParams(
                tagId = tagId,
                genre = destination.genre,
                year = destination.year,
                defaultSort = destination.sort,
                lockSort = lockSort,
            ),
        )
        mangaSortFilterController.initialize(
            initialParams = MangaSortFilterController.InitialParams(
                tagId = tagId,
                genre = destination.genre,
                year = destination.year,
                defaultSort = destination.sort,
                lockSort = lockSort,
            ),
        )

        val includeDescriptionFlow =
            MediaUtils.mediaViewOptionIncludeDescriptionFlow { mediaViewOption }

        collectSearch(
            searchType = SearchType.ANIME,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(1.seconds),
                    includeDescriptionFlow,
                    refresh.updates,
                    animeSortFilterController.filterParams,
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
            entry = { AnimeSearchEntry.Media(it) },
            filter = { animeSortFilterController.filterMedia(it) { it.media } },
            finalTransform = {
                applyMediaStatusChanges(
                    statusController = statusController,
                    ignoreController = ignoreController,
                    mediaFilteringData = settings.mediaFilteringData(false),
                    mediaFilterable = { it.entry.mediaFilterable },
                    copy = { copy(entry = entry.copy(mediaFilterable = it)) },
                )
            }
        )

        collectSearch(
            searchType = SearchType.MANGA,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(1.seconds),
                    includeDescriptionFlow,
                    refresh.updates,
                    mangaSortFilterController.filterParams,
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
            entry = { AnimeSearchEntry.Media(it) },
            filter = { mangaSortFilterController.filterMedia(it) { it.media } },
            finalTransform = {
                applyMediaStatusChanges(
                    statusController = statusController,
                    ignoreController = ignoreController,
                    mediaFilteringData = settings.mediaFilteringData(false),
                    mediaFilterable = { it.entry.mediaFilterable },
                    copy = { copy(entry = entry.copy(mediaFilterable = it)) },
                )
            }
        )

        collectSearch(
            searchType = SearchType.CHARACTER,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(1.seconds),
                    characterSortFilterController.filterParams,
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
                        sort = filterParams.sort.selectedOption(CharacterSortOption.SEARCH_MATCH)
                            .toApiValueForSearch(filterParams.sortAscending),
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
                    snapshotFlow { query }.debounce(1.seconds),
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
                    snapshotFlow { query }.debounce(1.seconds),
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
                    StudioListRow.Entry(
                        studio = it,
                        media = (it.main?.nodes?.filterNotNull().orEmpty()
                            .map(::MediaWithListStatusEntry) +
                                it.nonMain?.nodes?.filterNotNull().orEmpty()
                                    .map(::MediaWithListStatusEntry))
                            .distinctBy { it.media.id },
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
            searchType = SearchType.USER,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(1.seconds),
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
                        media = UserUtils.buildInitialMediaEntries(it),
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
                .flatMapLatest { snapshotFlow { selectedType } }
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

    enum class SearchType(val textRes: StringResource) {
        ANIME(Res.string.anime_search_type_anime),
        MANGA(Res.string.anime_search_type_manga),
        CHARACTER(Res.string.anime_search_type_character),
        STAFF(Res.string.anime_search_type_staff),
        STUDIO(Res.string.anime_search_type_studio),
        USER(Res.string.anime_search_type_user),
    }
}
