@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.seasonal

import android.os.SystemClock
import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.filter
import androidx.paging.map
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeMediaFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.FilterData
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchMediaPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SeasonalViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    val ignoreList: AnimeMediaIgnoreList,
) : ViewModel() {

    // TODO: Does this actually evict old pages from memory?
    private val pages = LruCache<Int, Page>(10)
    private val currentSeasonYear = AniListUtils.getCurrentSeasonYear()

    var initialPage = 0
    var tagShown by mutableStateOf<AnimeMediaFilterController.TagSection.Tag?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    val filterController =
        AnimeMediaFilterController(MediaSortOption::class, aniListApi, settings, ignoreList)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(type: Type) {
        initialPage = when (type) {
            Type.LAST -> Int.MAX_VALUE / 2 - 1
            Type.THIS -> Int.MAX_VALUE / 2
            Type.NEXT -> Int.MAX_VALUE / 2 + 1
        }
        val initialParams = AnimeMediaFilterController.InitialParams(
            isAnime = true,
            airingDateEnabled = false,
            filterData = FilterData(
                sortOption = MediaSortOption.POPULARITY,
                sortAscending = false,
            )
        )
        filterController.initialize(this, refreshUptimeMillis, initialParams)
    }

    fun onTagLongClick(tagId: String) {
        tagShown = filterController.tagsByCategory.value.values
            .asSequence()
            .mapNotNull { it.findTag(tagId) }
            .firstOrNull()
    }

    fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }

    @Composable
    fun items(page: Int): LazyPagingItems<MediaEntry> {
        var pageData = pages.get(page)
        if (pageData == null) {
            val seasonYear = AniListUtils.calculateSeasonYearWithOffset(
                seasonYear = currentSeasonYear,
                offset = Int.MAX_VALUE / 2 - page,
            )
            pageData = Page(seasonYear)
            pages.put(page, pageData)
        }

        return pageData.content.collectAsLazyPagingItems()
    }

    class MediaEntry(
        media: MediaAdvancedSearchQuery.Data.Page.Medium,
        ignored: Boolean,
    ) : AnimeMediaListRow.MediaEntry<MediaAdvancedSearchQuery.Data.Page.Medium>(media, ignored)

    enum class Type {
        LAST,
        THIS,
        NEXT,
    }

    inner class Page(seasonYear: Pair<MediaSeason, Int>) {
        var content = MutableStateFlow(PagingData.empty<MediaEntry>())

        init {
            viewModelScope.launch(CustomDispatchers.Main) {
                combine(
                    flowOf(""),
                    refreshUptimeMillis,
                    filterController.sortOptions,
                    filterController.sortAscending,
                    filterController.filterParams(),
                ) { query, requestMillis, sortOptions, sortAscending, filterParams ->
                    AnimeSearchMediaPagingSource.RefreshParams(
                        query = query,
                        requestMillis = requestMillis,
                        sortOptions = sortOptions,
                        sortAscending = sortAscending,
                        filterParams = filterParams,
                        seasonYearOverride = seasonYear,
                    )
                }
                    .debounce(100.milliseconds)
                    .distinctUntilChanged()
                    .flatMapLatest {
                        Pager(PagingConfig(pageSize = 10, enablePlaceholders = true)) {
                            AnimeSearchMediaPagingSource(aniListApi, it, MediaType.ANIME)
                        }.flow
                    }
                    .map {
                        // AniList can return duplicates across pages, manually enforce uniqueness
                        val seenIds = mutableSetOf<Int>()
                        it.filter { seenIds.add(it.id) }
                            .map { MediaEntry(it, ignored = ignoreList.get(it.id)) }
                    }
                    .cachedIn(viewModelScope)
                    .flatMapLatest { filterController.filterMedia(it) { it.media } }
                    .flowOn(CustomDispatchers.IO)
                    .collectLatest(content::emit)
            }
        }
    }
}
