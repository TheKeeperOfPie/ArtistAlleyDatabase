package com.thekeeperofpie.artistalleydatabase.anime.search

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
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
import com.anilist.CharacterAdvancedSearchQuery
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.StaffSearchQuery
import com.anilist.StudioSearchQuery
import com.anilist.UserSearchQuery
import com.anilist.type.MediaFormat
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.anilist.type.StudioSort
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
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
import com.thekeeperofpie.artistalleydatabase.anime.utils.filterOnIO
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class AnimeSearchViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    private val statusController: MediaListStatusController,
    val ignoreController: IgnoreController,
    mediaTagsController: MediaTagsController,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    featureOverrideProvider: FeatureOverrideProvider,
    private val monetizationController: MonetizationController,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)
    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    var content = MutableStateFlow(PagingData.empty<AnimeSearchEntry>())

    val unlocked = monetizationController.unlocked

    private var initialized = false
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
        scope = scope,
        sortTypeEnumClass = MediaSortOption::class,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController,
        mediaGenresController = mediaGenresController,
        mediaLicensorsController = mediaLicensorsController,
        userScoreEnabled = false,
    ) {

        override val suggestionsSection = SortFilterSection.Suggestions(
            titleRes = R.string.anime_media_filter_suggestions_label,
            titleDropdownContentDescriptionRes = R.string.anime_media_filter_suggestions_expand_content_description,
            suggestions = AnimeFilterSuggestion.entries.toList(),
            onSuggestionClick = {
                when (it) {
                    AnimeFilterSuggestion.RECENT_UNTRACKED -> {
                        sortSection.changeSelected(
                            defaultEnabled = MediaSortOption.END_DATE,
                            sortAscending = false,
                            lockSort = false,
                        )
                        statusSection.setIncluded(MediaStatus.FINISHED, locked = false)
                        formatSection.setIncluded(MediaFormat.TV, locked = false)
                        listStatusSection.setExcluded(null, locked = false)
                        airingDate = AiringDate.Basic() to airingDate.second
                        airingDateIsAdvanced = false
                    }
                    AnimeFilterSuggestion.LAST_SEASON -> {
                        sortSection.changeSelected(
                            defaultEnabled = MediaSortOption.POPULARITY,
                            sortAscending = false,
                            lockSort = false,
                        )
                        statusSection.clear()
                        formatSection.setIncluded(MediaFormat.TV, locked = false)
                        val (_, year) = AniListUtils.getPreviousSeasonYear()
                        airingDate = AiringDate.Basic(
                            AiringDate.SeasonOption.PREVIOUS,
                            year.toString(),
                        ) to airingDate.second
                        airingDateIsAdvanced = false
                        listStatusSection.clear()
                    }
                    AnimeFilterSuggestion.CURRENT_SEASON -> {
                        sortSection.changeSelected(
                            defaultEnabled = MediaSortOption.POPULARITY,
                            sortAscending = false,
                            lockSort = false,
                        )
                        statusSection.clear()
                        formatSection.setIncluded(MediaFormat.TV, locked = false)
                        val (_, year) = AniListUtils.getCurrentSeasonYear()
                        airingDate = AiringDate.Basic(
                            AiringDate.SeasonOption.CURRENT,
                            year.toString(),
                        ) to airingDate.second
                        airingDateIsAdvanced = false
                        listStatusSection.clear()
                    }
                    AnimeFilterSuggestion.NEXT_SEASON -> {
                        sortSection.changeSelected(
                            defaultEnabled = MediaSortOption.POPULARITY,
                            sortAscending = false,
                            lockSort = false,
                        )
                        statusSection.clear()
                        formatSection.setIncluded(MediaFormat.TV, locked = false)
                        val (_, year) = AniListUtils.getNextSeasonYear()
                        airingDate = AiringDate.Basic(
                            AiringDate.SeasonOption.NEXT,
                            year.toString(),
                        ) to airingDate.second
                        airingDateIsAdvanced = false
                        listStatusSection.clear()
                    }
                }
            }
        )

        enum class AnimeFilterSuggestion(private val textRes: Int) :
            SortFilterSection.Suggestions.Suggestion {
            RECENT_UNTRACKED(R.string.anime_media_filter_suggestions_recent_untracked),
            LAST_SEASON(R.string.anime_media_filter_suggestions_last_season),
            CURRENT_SEASON(R.string.anime_media_filter_suggestions_current_season),
            NEXT_SEASON(R.string.anime_media_filter_suggestions_next_season),
            ;

            @Composable
            override fun text() = stringResource(textRes)
        }
    }

    val mangaSortFilterController = MangaSortFilterController(
        sortTypeEnumClass = MediaSortOption::class,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController,
        mediaGenresController = mediaGenresController,
        mediaLicensorsController = mediaLicensorsController,
        userScoreEnabled = false,
    )

    val characterSortFilterController =
        CharacterSortFilterController(settings, featureOverrideProvider)

    val staffSortFilterController = StaffSortFilterController(settings, featureOverrideProvider)

    val studioSortFilterController = StudioSortFilterController(settings, featureOverrideProvider)

    val userSortFilterController = UserSortFilterController(settings, featureOverrideProvider)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    var selectedType by mutableStateOf(
        if (settings.preferredMediaType.value == MediaType.ANIME) {
            SearchType.ANIME
        } else {
            SearchType.MANGA
        }
    )
    private val results =
        LoadStates(
            LoadState.Loading,
            LoadState.NotLoading(true),
            LoadState.NotLoading(true),
        ).let { loadStates ->
            SearchType.values().map {
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

        val includeDescriptionFlow =
            MediaUtils.mediaViewOptionIncludeDescriptionFlow { mediaViewOption }

        collectSearch(
            searchType = SearchType.ANIME,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(500.milliseconds),
                    includeDescriptionFlow,
                    refreshUptimeMillis,
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
                    settings = settings,
                    media = { it.media },
                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                        AnimeSearchEntry.Media(
                            media = media,
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

        collectSearch(
            searchType = SearchType.MANGA,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(500.milliseconds),
                    includeDescriptionFlow,
                    refreshUptimeMillis,
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
                    settings = settings,
                    media = { it.media },
                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                        AnimeSearchEntry.Media(
                            media = media,
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

        collectSearch(
            searchType = SearchType.CHARACTER,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(500.milliseconds),
                    characterSortFilterController.filterParams,
                    refreshUptimeMillis,
                    ::Triple
                )
            },
            pagingSource = { (query, filterParams), cache: LruCache<Int, PagingSource.LoadResult.Page<Int, CharacterAdvancedSearchQuery.Data.Page.Character>> ->
                AniListPagingSource(cache = cache) {
                    aniListApi.searchCharacters(
                        query = query,
                        page = it,
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
                        settings.showIgnored,
                        settings.showAdult,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { statuses, _, showIgnored, showAdult, showLessImportantTags, showSpoilerTags ->
                        it.mapNotNull {
                            it.copy(entry = it.entry.copy(media = it.entry.media.mapNotNull {
                                applyMediaFiltering(
                                    statuses = statuses,
                                    ignoreController = ignoreController,
                                    showAdult = showAdult,
                                    showIgnored = showIgnored,
                                    showLessImportantTags = showLessImportantTags,
                                    showSpoilerTags = showSpoilerTags,
                                    entry = it,
                                    transform = { it },
                                    media = it.media,
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
                    snapshotFlow { query }.debounce(500.milliseconds),
                    staffSortFilterController.filterParams,
                    refreshUptimeMillis,
                    ::Triple
                )
            },
            pagingSource = { (query, filterParams), cache: LruCache<Int, PagingSource.LoadResult.Page<Int, StaffSearchQuery.Data.Page.Staff>> ->
                AniListPagingSource(cache = cache) {
                    aniListApi.searchStaff(
                        query = query,
                        page = it,
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
                        settings.showIgnored,
                        settings.showAdult,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { statuses, _, showIgnored, showAdult, showLessImportantTags, showSpoilerTags ->
                        it.mapNotNull {
                            it.copy(entry = it.entry.copy(media = it.entry.media.mapNotNull {
                                applyMediaFiltering(
                                    statuses = statuses,
                                    ignoreController = ignoreController,
                                    showAdult = showAdult,
                                    showIgnored = showIgnored,
                                    showLessImportantTags = showLessImportantTags,
                                    showSpoilerTags = showSpoilerTags,
                                    entry = it,
                                    transform = { it },
                                    media = it.media,
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
                    snapshotFlow { query }.debounce(500.milliseconds),
                    refreshUptimeMillis,
                    studioSortFilterController.filterParams,
                    ::Triple,
                )
            },
            pagingSource = { (query, _, filterParams), cache: LruCache<Int, PagingSource.LoadResult.Page<Int, StudioSearchQuery.Data.Page.Studio>> ->
                AniListPagingSource(cache = cache) {
                    aniListApi.searchStudios(
                        query = query,
                        page = it,
                        perPage = 10,
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
                        media = it.main?.nodes?.filterNotNull().orEmpty()
                            .map(::MediaWithListStatusEntry) +
                                it.nonMain?.nodes?.filterNotNull().orEmpty()
                                    .map(::MediaWithListStatusEntry),
                    )
                )
            },
            finalTransform = {
                flatMapLatest {
                    combine(
                        statusController.allChanges(),
                        ignoreController.updates(),
                        settings.showIgnored,
                        settings.showAdult,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { statuses, _, showIgnored, showAdult, showLessImportantTags, showSpoilerTags ->
                        it.mapNotNull {
                            it.copy(entry = it.entry.copy(media = it.entry.media.mapNotNull {
                                applyMediaFiltering(
                                    statuses = statuses,
                                    ignoreController = ignoreController,
                                    showAdult = showAdult,
                                    showIgnored = showIgnored,
                                    showLessImportantTags = showLessImportantTags,
                                    showSpoilerTags = showSpoilerTags,
                                    entry = it,
                                    transform = { it },
                                    media = it.media,
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
                    snapshotFlow { query }.debounce(500.milliseconds),
                    userSortFilterController.filterParams,
                    refreshUptimeMillis,
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
                        settings.showIgnored,
                        settings.showAdult,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { statuses, _, showIgnored, showAdult, showLessImportantTags, showSpoilerTags ->
                        it.mapNotNull {
                            it.copy(entry = it.entry.copy(media = it.entry.media.mapNotNull {
                                applyMediaFiltering(
                                    statuses = statuses,
                                    ignoreController = ignoreController,
                                    showAdult = showAdult,
                                    showIgnored = showIgnored,
                                    showLessImportantTags = showLessImportantTags,
                                    showSpoilerTags = showSpoilerTags,
                                    entry = it,
                                    transform = { it },
                                    media = it.media,
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
                            pageSize = 10,
                            initialLoadSize = 10,
                            jumpThreshold = 20,
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

    fun initialize(
        defaultMediaSort: MediaSortOption,
        genre: String? = null,
        year: Int? = null,
        searchType: SearchType? = null,
        lockSort: Boolean,
    ) {
        if (initialized) return
        initialized = true
        if (searchType != null) {
            this.selectedType = searchType
        }
        animeSortFilterController.initialize(
            viewModel = this,
            refreshUptimeMillis = refreshUptimeMillis,
            initialParams = AnimeSortFilterController.InitialParams(
                tagId = tagId,
                genre = genre,
                year = year,
                defaultSort = defaultMediaSort,
                lockSort = lockSort,
            ),
        )
        mangaSortFilterController.initialize(
            viewModel = this,
            refreshUptimeMillis = refreshUptimeMillis,
            initialParams = MangaSortFilterController.InitialParams(
                tagId = tagId,
                genre = genre,
                year = year,
                defaultSort = defaultMediaSort,
                lockSort = lockSort,
            ),
        )
    }

    fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }

    enum class SearchType(@StringRes val textRes: Int) {
        ANIME(R.string.anime_search_type_anime),
        MANGA(R.string.anime_search_type_manga),
        CHARACTER(R.string.anime_search_type_character),
        STAFF(R.string.anime_search_type_staff),
        STUDIO(R.string.anime_search_type_studio),
        USER(R.string.anime_search_type_user),
    }
}
