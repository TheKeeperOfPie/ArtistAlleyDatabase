package com.thekeeperofpie.artistalleydatabase.anime.search

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.anilist.MediaAdvancedSearchQuery.Data.Page.Medium
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
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
import kotlinx.coroutines.flow.flowOf
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
    private val ignoreList: AnimeMediaIgnoreList,
) : ViewModel() {

    var query by mutableStateOf("")
    var content = MutableStateFlow(PagingData.empty<AnimeSearchEntry>())
    var tagShown by mutableStateOf<AnimeMediaFilterController.TagSection.Tag?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    private var initialized = false

    private val filterController =
        AnimeMediaFilterController(MediaSortOption::class, aniListApi, settings, ignoreList)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    var selectedType by mutableStateOf(SearchType.ANIME)
    private val results =
        LoadStates(
            LoadState.Loading,
            LoadState.NotLoading(true),
            LoadState.NotLoading(true),
        ).let { loadStates ->
            SearchType.values().map {
                it to when (it) {
                    SearchType.ANIME,
                    SearchType.MANGA -> PagingData.empty<AnimeSearchEntry.Media<Medium>>(loadStates)
                    SearchType.CHARACTER -> PagingData.empty<AnimeSearchEntry.Character>(loadStates)
                    SearchType.STAFF -> PagingData.empty<AnimeSearchEntry.Staff>(loadStates)
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

        collectSearch(
            searchType = SearchType.ANIME,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(500.milliseconds),
                    refreshUptimeMillis,
                    filterController.sortOptions,
                    filterController.sortAscending,
                    filterController.filterParams(),
                    AnimeSearchMediaPagingSource::RefreshParams
                )
            },
            pagingSource = { AnimeSearchMediaPagingSource(aniListApi, it, MediaType.ANIME) },
            id = { it.id },
            entry = { AnimeSearchEntry.Media(it, ignored = ignoreList.get(it.id)) },
            filter = ::filterMedia,
        )

        collectSearch(
            searchType = SearchType.MANGA,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(500.milliseconds),
                    refreshUptimeMillis,
                    filterController.sortOptions,
                    filterController.sortAscending,
                    filterController.filterParams(),
                    AnimeSearchMediaPagingSource::RefreshParams
                )
            },
            pagingSource = { AnimeSearchMediaPagingSource(aniListApi, it, MediaType.MANGA) },
            id = { it.id },
            entry = { AnimeSearchEntry.Media(it, ignored = ignoreList.get(it.id)) },
            filter = ::filterMedia,
        )

        collectSearch(
            searchType = SearchType.CHARACTER,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(500.milliseconds),
                    refreshUptimeMillis,
                ) { query, requestMillis ->
                    AnimeSearchCharacterPagingSource.RefreshParams(
                        query = query,
                        requestMillis = requestMillis,
                        sortOptions = emptyList(),
                        sortAscending = false,
                        // TODO: Actually hook up filters
                    )
                }
            },
            pagingSource = { AnimeSearchCharacterPagingSource(aniListApi, it) },
            id = { it.id },
            entry = { AnimeSearchEntry.Character(it) },
        )

        collectSearch(
            searchType = SearchType.STAFF,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(500.milliseconds),
                    refreshUptimeMillis,
                ) { query, requestMillis ->
                    AnimeSearchStaffPagingSource.RefreshParams(
                        query = query,
                        requestMillis = requestMillis,
                        sortOptions = emptyList(),
                        sortAscending = false,
                        // TODO: Actually hook up filters
                    )
                }
            },
            pagingSource = { AnimeSearchStaffPagingSource(aniListApi, it) },
            id = { it.id },
            entry = { AnimeSearchEntry.Staff(it) },
        )

        collectSearch(
            searchType = SearchType.USER,
            flow = {
                combine(
                    snapshotFlow { query }.debounce(500.milliseconds),
                    refreshUptimeMillis,
                ) { query, requestMillis ->
                    AnimeSearchUserPagingSource.RefreshParams(
                        query = query,
                        requestMillis = requestMillis,
                        sortOptions = emptyList(),
                        sortAscending = false,
                        // TODO: Actually hook up filters
                    )
                }
            },
            pagingSource = { AnimeSearchUserPagingSource(aniListApi, it) },
            id = { it.id },
            entry = { AnimeSearchEntry.User(it) },
        )
    }

    private fun <Params, Result : Any, Entry : AnimeSearchEntry> collectSearch(
        searchType: SearchType,
        flow: () -> Flow<Params>,
        pagingSource: (Params) -> PagingSource<Int, Result>,
        id: (Result) -> Int,
        entry: (Result) -> Entry,
        filter: ((PagingData<Entry>) -> Flow<PagingData<Entry>>)? = null
    ) {
        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { selectedType }
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
                    it.filter { seenIds.add(id(it)) }.map { entry(it) }
                }
                .cachedIn(viewModelScope)
                .run {
                    if (filter != null) flatMapLatest { filter(it) } else this
                }
                .collectLatest {
                    results[searchType]!!.emit(it)
                }
        }
    }

    private fun filterMedia(result: PagingData<AnimeSearchEntry.Media<Medium>>) =
        combine(
            flowOf(result),
            filterController.showIgnored,
            filterController.listStatuses
        ) { pagingData, showIgnored, listStatuses ->
            val includes = listStatuses
                .filter { it.state == FilterIncludeExcludeState.INCLUDE }
                .map { it.value }
            val excludes = listStatuses
                .filter { it.state == FilterIncludeExcludeState.EXCLUDE }
                .map { it.value }
            pagingData.filter {
                val listStatus = it.media.mediaListEntry?.status
                if (excludes.isNotEmpty() && excludes.contains(listStatus)) {
                    return@filter false
                }

                if (includes.isNotEmpty() && !includes.contains(listStatus)) {
                    return@filter false
                }

                if (showIgnored) true else !ignoreList.get(it.id.valueId)
            }
        }

    fun initialize(filterParams: AnimeMediaFilterController.InitialParams) {
        if (initialized) return
        initialized = true
        filterController.initialize(this, refreshUptimeMillis, filterParams)
    }

    fun filterData() = filterController.data()

    fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }

    fun onTagLongClick(tagId: String) {
        tagShown = filterController.tagsByCategory.value.values
            .asSequence()
            .mapNotNull { it.findTag(tagId) }
            .firstOrNull()
    }

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry) {
        val mediaId = entry.id?.valueId ?: return
        val ignored = !entry.ignored
        ignoreList.set(mediaId, ignored)
        entry.ignored = ignored
    }

    enum class SearchType(@StringRes val textRes: Int) {
        ANIME(R.string.anime_search_type_anime),
        MANGA(R.string.anime_search_type_manga),
        CHARACTER(R.string.anime_search_type_character),
        STAFF(R.string.anime_search_type_staff),
        USER(R.string.anime_search_type_user),
    }
}
