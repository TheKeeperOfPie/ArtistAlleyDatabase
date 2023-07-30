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
import androidx.paging.map
import com.anilist.MediaAdvancedSearchQuery
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.TagSection
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchMediaPagingSource
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIntIds
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
    private val settings: AnimeSettings,
    private val ignoreList: AnimeMediaIgnoreList,
    private val statusController: MediaListStatusController,
    private val mediaTagsController: MediaTagsController,
    featureOverrideProvider: FeatureOverrideProvider,
) : ViewModel() {

    val viewer = aniListApi.authedUser

    // TODO: Does this actually evict old pages from memory?
    private val pages = LruCache<Int, Page>(10)
    private val currentSeasonYear = AniListUtils.getCurrentSeasonYear()

    var initialPage = 0
    var tagShown by mutableStateOf<TagSection.Tag?>(null)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    val sortFilterController = AnimeSortFilterController(
        sortTypeEnumClass = MediaSortOption::class,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController
    )

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun initialize(type: Type) {
        initialPage = when (type) {
            Type.LAST -> Int.MAX_VALUE / 2 - 1
            Type.THIS -> Int.MAX_VALUE / 2
            Type.NEXT -> Int.MAX_VALUE / 2 + 1
        }

        sortFilterController.initialize(
            viewModel = this,
            refreshUptimeMillis = refreshUptimeMillis,
            initialParams = AnimeSortFilterController.InitialParams(
                airingDateEnabled = false,
                defaultSort = MediaSortOption.POPULARITY,
            ),
        )
    }

    fun onTagLongClick(tagId: String) {
        tagShown = mediaTagsController.tags.value.values
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

    fun onMediaLongClick(entry: AnimeMediaListRow.Entry<*>) =
        ignoreList.toggle(entry.media.id.toString())

    class MediaEntry(
        media: MediaAdvancedSearchQuery.Data.Page.Medium,
        mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        progress: Int? = null,
        progressVolumes: Int? = null,
        ignored: Boolean = false,
    ) : AnimeMediaListRow.Entry<MediaAdvancedSearchQuery.Data.Page.Medium>(
        media = media,
        mediaListStatus = mediaListStatus,
        progress = progress,
        progressVolumes = progressVolumes,
        ignored = ignored,
    )

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
                    sortFilterController.filterParams(),
                ) { query, requestMillis, filterParams ->
                    AnimeSearchMediaPagingSource.RefreshParams(
                        query = query,
                        requestMillis = requestMillis,
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
                    .enforceUniqueIntIds { it.id }
                    .map { it.map { MediaEntry(it) } }
                    .cachedIn(viewModelScope)
                    .applyMediaStatusChanges(
                        statusController = statusController,
                        ignoreList = ignoreList,
                        settings = settings,
                        media = { it.media },
                        copy = { mediaListStatus, progress, progressVolumes, ignored ->
                            MediaEntry(
                                media = media,
                                mediaListStatus = mediaListStatus,
                                progress = progress,
                                progressVolumes = progressVolumes,
                                ignored = ignored,
                            )
                        },
                    )
                    .flatMapLatest { sortFilterController.filterMedia(it) { it.media } }
                    .cachedIn(viewModelScope)
                    .flowOn(CustomDispatchers.IO)
                    .collectLatest(content::emit)
            }
        }
    }
}
