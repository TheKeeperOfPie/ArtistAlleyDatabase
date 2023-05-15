package com.thekeeperofpie.artistalleydatabase.anime.search

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.anilist.MediaAdvancedSearchQuery.Data.Page.Medium
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.combine
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.utils.IncludeExcludeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnimeSearchViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    private val ignoreList: AnimeMediaIgnoreList,
) : ViewModel() {

    var query by mutableStateOf("")
    var content = MutableStateFlow(PagingData.empty<AnimeMediaListRow.MediaEntry>())
    var tagShown by mutableStateOf<AnimeMediaFilterController.TagSection.Tag?>(null)

    private var initialized = false

    private val filterController =
        AnimeMediaFilterController(MediaSortOption::class, aniListApi, settings)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            @OptIn(FlowPreview::class)
            combine(
                snapshotFlow { query }.debounce(500.milliseconds),
                refreshUptimeMillis,
                filterController.sortOptions,
                filterController.sortAscending,
                filterController.genres,
                filterController.tagsByCategory,
                filterController.tagRank(),
                filterController.statuses,
                filterController.formats,
                filterController.showAdult,
                filterController.onListOptions,
                filterController.averageScoreRange,
                filterController.episodesRange,
                filterController.airingDate(),
                filterController.sources,
                AnimeMediaSearchPagingSource::RefreshParams
            )
                .flowOn(CustomDispatchers.IO)
                .debounce(100.milliseconds)
                .flatMapLatest {
                    Pager(PagingConfig(pageSize = 10, enablePlaceholders = true)) {
                        AnimeMediaSearchPagingSource(aniListApi, it)
                    }.flow
                }
                .map {
                    // AniList can return duplicates across pages, manually enforce uniqueness
                    val seenIds = mutableSetOf<Int>()
                    it.filter { seenIds.add(it.id) }
                        .map<Medium, AnimeMediaListRow.MediaEntry> {
                            AnimeMediaListRow.MediaEntry(it, ignored = ignoreList.get(it.id))
                        }
                }
                .cachedIn(viewModelScope)
                .flatMapLatest {
                    combine(flowOf(it), filterController.showIgnored, filterController.listStatuses) { pagingData, showIgnored, listStatuses ->
                        val includes = listStatuses
                            .filter { it.state == IncludeExcludeState.INCLUDE }
                            .map { it.value }
                        val excludes = listStatuses
                            .filter { it.state == IncludeExcludeState.EXCLUDE }
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
                }
                .collectLatest(content::emit)
        }
    }

    fun initialize(filterParams: AnimeMediaFilterController.InitialParams) {
        if (initialized) return
        initialized = true
        filterController.initialize(this, refreshUptimeMillis, filterParams)
    }

    fun filterData() = filterController.data()

    fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }

    fun onQuery(query: String) {
        this.query = query
    }

    fun onTagDismiss() {
        tagShown = null
    }

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
}
