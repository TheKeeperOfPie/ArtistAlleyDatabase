package com.thekeeperofpie.artistalleydatabase.anime.search

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.anilist.type.MediaType
import com.anilist.type.StudioSort
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MangaSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaGenresController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaViewOption
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.user.UserListRow
import com.thekeeperofpie.artistalleydatabase.anime.user.UserSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.utils.filterOnIO
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.monetization.MonetizationController
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.flow.transformWhile
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
) : ViewModel() {

    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)
    val viewer = aniListApi.authedUser
    var query by mutableStateOf("")
    var content = MutableStateFlow(PagingData.empty<AnimeSearchEntry>())

    val unlocked = monetizationController.unlocked

    private var initialized = false

    val animeSortFilterController = AnimeSortFilterController(
        sortTypeEnumClass = MediaSortOption::class,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController,
        mediaGenresController = mediaGenresController,
        mediaLicensorsController = mediaLicensorsController,
    )

    val mangaSortFilterController = MangaSortFilterController(
        sortTypeEnumClass = MediaSortOption::class,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController,
        mediaGenresController = mediaGenresController,
        mediaLicensorsController = mediaLicensorsController,
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

        val includeDescriptionFlow = snapshotFlow { mediaViewOption }
            .map { it == MediaViewOption.LARGE_CARD }
            .transformWhile {
                // Take until description is ever requested,
                // then always request to prevent unnecessary refreshes
                emit(it)
                !it
            }
            .distinctUntilChanged()

        collectSearch(
            searchType = SearchType.ANIME,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(500.milliseconds),
                    includeDescriptionFlow,
                    refreshUptimeMillis,
                    animeSortFilterController.filterParams(),
                    AnimeSearchMediaPagingSource::RefreshParams
                )
            },
            pagingSource = { AnimeSearchMediaPagingSource(aniListApi, it, MediaType.ANIME) },
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
                    mangaSortFilterController.filterParams(),
                    AnimeSearchMediaPagingSource::RefreshParams
                )
            },
            pagingSource = { AnimeSearchMediaPagingSource(aniListApi, it, MediaType.MANGA) },
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
                    refreshUptimeMillis,
                    characterSortFilterController.filterParams(),
                    AnimeSearchCharacterPagingSource::RefreshParams
                )
            },
            pagingSource = { AnimeSearchCharacterPagingSource(aniListApi, it) },
            id = { it.id },
            entry = {
                AnimeSearchEntry.Character(
                    CharacterListRow.Entry(
                        character = it,
                        media = it.media?.edges?.mapNotNull { it?.node }.orEmpty()
                            .distinctBy { it.id }
                            .map {
                                CharacterListRow.Entry.MediaEntry(
                                    media = it,
                                    isAdult = it.isAdult
                                )
                            }
                    )
                )
            },
            finalTransform = {
                flatMapLatest { pagingData ->
                    combine(
                        ignoreController.updates(),
                        settings.showIgnored,
                        settings.showAdult,
                    ) { _, showIgnored, showAdult ->
                        pagingData.mapOnIO {
                            it.copy(entry = it.entry.copy(
                                media = it.entry.media
                                    .filter { showAdult || it.isAdult == false }
                                    .map {
                                        it.copy(
                                            ignored = ignoreController.isIgnored(
                                                it.media.id.toString()
                                            )
                                        )
                                    }
                                    .filter { showIgnored || !it.ignored }
                            ))
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
                    refreshUptimeMillis,
                    staffSortFilterController.filterParams(),
                    AnimeSearchStaffPagingSource::RefreshParams
                )
            },
            pagingSource = { AnimeSearchStaffPagingSource(aniListApi, it) },
            id = { it.id },
            entry = {
                AnimeSearchEntry.Staff(
                    StaffListRow.Entry(
                        staff = it,
                        media = it.staffMedia?.nodes?.filterNotNull().orEmpty()
                            .distinctBy { it.id }
                            .map { StaffListRow.Entry.MediaEntry(media = it, isAdult = it.isAdult) }
                    )
                )
            },
            finalTransform = {
                flatMapLatest { pagingData ->
                    combine(
                        ignoreController.updates(),
                        settings.showIgnored,
                        settings.showAdult,
                    ) { _, showIgnored, showAdult ->
                        pagingData.mapOnIO {
                            it.copy(entry = it.entry.copy(
                                media = it.entry.media
                                    .filter { showAdult || it.isAdult == false }
                                    .map {
                                        it.copy(
                                            ignored = ignoreController.isIgnored(
                                                it.media.id.toString()
                                            )
                                        )
                                    }
                                    .filter { showIgnored || !it.ignored }
                            ))
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
                    studioSortFilterController.filterParams(),
                    ::Triple,
                )
            },
            pagingSource = { (query, _, filterParams) ->
                AniListPagingSource {
                    val result = aniListApi.searchStudios(
                        query = query,
                        page = it,
                        perPage = 10,
                        sort = filterParams.sort.filter { it.state == FilterIncludeExcludeState.INCLUDE }
                            .flatMap { it.value.toApiValue(filterParams.sortAscending) }
                            .ifEmpty { listOf(StudioSort.SEARCH_MATCH) }
                    )

                    result.page.pageInfo to result.page.studios?.filterNotNull().orEmpty()
                }
            },
            id = { it.id },
            entry = {
                AnimeSearchEntry.Studio(
                    StudioListRow.Entry(
                        studio = it,
                        media = it.media?.nodes?.filterNotNull().orEmpty()
                            .distinctBy { it.id }
                            .map {
                                StudioListRow.Entry.MediaEntry(
                                    media = it,
                                    isAdult = it.isAdult
                                )
                            }
                    )
                )
            },
            finalTransform = {
                flatMapLatest { pagingData ->
                    combine(
                        ignoreController.updates(),
                        settings.showIgnored,
                        settings.showAdult,
                    ) { _, showIgnored, showAdult ->
                        pagingData.mapOnIO {
                            it.copy(entry = it.entry.copy(
                                media = it.entry.media
                                    .filter { showAdult || it.isAdult == false }
                                    .map {
                                        it.copy(
                                            ignored = ignoreController.isIgnored(
                                                it.media.id.toString()
                                            )
                                        )
                                    }
                                    .filter { showIgnored || !it.ignored }
                            ))
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
                    refreshUptimeMillis,
                    userSortFilterController.filterParams(),
                    AnimeSearchUserPagingSource::RefreshParams
                )
            },
            pagingSource = { AnimeSearchUserPagingSource(aniListApi, it) },
            id = { it.id },
            entry = {
                val anime = it.favourites?.anime?.edges
                    ?.filterNotNull()
                    ?.sortedBy { it.favouriteOrder }
                    ?.mapNotNull { it.node }
                    .orEmpty()
                    .map { UserListRow.Entry.MediaEntry(media = it, isAdult = it.isAdult) }

                val manga = it.favourites?.manga?.edges
                    ?.filterNotNull()
                    ?.sortedBy { it.favouriteOrder }
                    ?.mapNotNull { it.node }
                    .orEmpty()
                    .map { UserListRow.Entry.MediaEntry(media = it, isAdult = it.isAdult) }
                AnimeSearchEntry.User(
                    UserListRow.Entry(
                        user = it,
                        media = (anime + manga).distinctBy { it.media.id },
                    )
                )
            },
            finalTransform = {
                flatMapLatest { pagingData ->
                    combine(
                        ignoreController.updates(),
                        settings.showIgnored,
                        settings.showAdult,
                    ) { _, showIgnored, showAdult ->
                        pagingData.mapOnIO {
                            it.copy(entry = it.entry.copy(
                                media = it.entry.media
                                    .filter { showAdult || it.isAdult == false }
                                    .map {
                                        it.copy(
                                            ignored = ignoreController.isIgnored(
                                                it.media.id.toString()
                                            )
                                        )
                                    }
                                    .filter { showIgnored || !it.ignored }
                            ))
                        }
                    }
                }
            }
        )
    }

    private fun <Params, Result : Any, Entry : AnimeSearchEntry> collectSearch(
        searchType: SearchType,
        flow: () -> Flow<Params>,
        pagingSource: (Params) -> PagingSource<Int, Result>,
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
                    Pager(PagingConfig(pageSize = 10, enablePlaceholders = true)) {
                        pagingSource(it)
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
        tagId: String? = null,
        genre: String? = null,
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
