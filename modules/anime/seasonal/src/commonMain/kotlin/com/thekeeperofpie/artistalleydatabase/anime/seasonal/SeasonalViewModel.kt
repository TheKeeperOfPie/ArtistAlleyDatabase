package com.thekeeperofpie.artistalleydatabase.anime.seasonal

import androidx.collection.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.anilist.data.MediaAdvancedSearchQuery
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.fragment.MediaPreviewWithDescription
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.search.data.AnimeSearchMediaPagingSource
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItemsWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@Inject
class SeasonalViewModel<SortFilterControllerType : SortFilterController<MediaSearchFilterParams<MediaSortOption>>, MediaEntry : Any>(
    private val aniListApi: AuthedAniListApi,
    private val settings: MediaDataSettings,
    val ignoreController: IgnoreController,
    private val mediaListStatusController: MediaListStatusController,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val sortFilterControllerProvider: (CoroutineScope) -> SortFilterControllerType,
    @Assisted private val filterMedia: (
        sortFilterController: SortFilterControllerType,
        PagingData<MediaEntry>,
        (MediaEntry) -> MediaPreview,
    ) -> Flow<PagingData<MediaEntry>>,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaEntry>,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<SeasonalDestinations.Seasonal>(navigationTypeMap)

    val viewer = aniListApi.authedUser
    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)

    // TODO: Does this actually evict old pages from memory?
    private val pages = LruCache<Int, Page>(10)
    private val currentSeasonYear = AniListUtils.getCurrentSeasonYear()

    var initialPage = when (destination.type) {
        SeasonalDestinations.Seasonal.Type.LAST -> Int.MAX_VALUE / 2 - 1
        SeasonalDestinations.Seasonal.Type.THIS -> Int.MAX_VALUE / 2
        SeasonalDestinations.Seasonal.Type.NEXT -> Int.MAX_VALUE / 2 + 1
    }

    private val refresh = RefreshFlow()

    // TODO: All of the logic for this is extremely messy
    val sortFilterController = sortFilterControllerProvider(viewModelScope)

    fun onRefresh() = refresh.refresh()

    @Composable
    fun items(page: Int): LazyPagingItems<MediaEntry> {
        var pageData = pages[page]
        if (pageData == null) {
            val seasonYear = AniListUtils.calculateSeasonYearWithOffset(
                seasonYear = currentSeasonYear,
                offset = Int.MAX_VALUE / 2 - page,
            )
            pageData = Page(seasonYear)
            pages.put(page, pageData)
        }

        return pageData.content.collectAsLazyPagingItemsWithLifecycle()
    }

    inner class Page(seasonYear: Pair<MediaSeason, Int>) {
        var content = MutableStateFlow(PagingData.empty<MediaEntry>())

        init {
            viewModelScope.launch(CustomDispatchers.Main) {
                combine(
                    MediaDataUtils.mediaViewOptionIncludeDescriptionFlow { mediaViewOption },
                    refresh.updates,
                    sortFilterController.filterParams,
                ) { includeDescription, refreshEvent, filterParams ->
                    AnimeSearchMediaPagingSource.RefreshParams(
                        query = "",
                        includeDescription = includeDescription,
                        refreshEvent = refreshEvent,
                        filterParams = filterParams,
                        seasonYearOverride = seasonYear,
                    )
                }
                    .distinctUntilChanged()
                    .flatMapLatest {
                        val cache =
                            LruCache<Int, PagingSource.LoadResult.Page<Int, MediaAdvancedSearchQuery.Data.Page.Medium>>(
                                20
                            )
                        Pager(PagingConfig(pageSize = 10, enablePlaceholders = true)) {
                            AnimeSearchMediaPagingSource(
                                aniListApi = aniListApi,
                                refreshParams = it,
                                cache = cache,
                                mediaType = MediaType.ANIME,
                            )
                        }.flow
                    }
                    .enforceUniqueIntIds { it.id }
                    .map { it.mapOnIO(mediaEntryProvider::mediaEntry) }
                    .cachedIn(viewModelScope)
                    .flatMapLatest {
                        filterMedia(sortFilterController, it, mediaEntryProvider::media)
                    }
                    .applyMediaStatusChanges(
                        statusController = mediaListStatusController,
                        ignoreController = ignoreController,
                        mediaFilteringData = settings.mediaFilteringData(false),
                        mediaFilterable = mediaEntryProvider::mediaFilterable,
                        copy = { mediaEntryProvider.copyMediaEntry(this, it) },
                    )
                    .cachedIn(viewModelScope)
                    .flowOn(CustomDispatchers.IO)
                    .collectLatest(content::emit)
            }
        }
    }

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val settings: MediaDataSettings,
        private val ignoreController: IgnoreController,
        private val mediaListStatusController: MediaListStatusController,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {
        fun <SortFilterControllerType : SortFilterController<MediaSearchFilterParams<MediaSortOption>>, MediaEntry : Any> create(
            mediaEntryProvider: MediaEntryProvider<MediaPreviewWithDescription, MediaEntry>,
            sortFilterControllerProvider: (CoroutineScope) -> SortFilterControllerType,
            filterMedia: (
                sortFilterController: SortFilterControllerType,
                PagingData<MediaEntry>,
                (MediaEntry) -> MediaPreview,
            ) -> Flow<PagingData<MediaEntry>>,
        ) = SeasonalViewModel(
            aniListApi = aniListApi,
            settings = settings,
            ignoreController = ignoreController,
            mediaListStatusController = mediaListStatusController,
            navigationTypeMap = navigationTypeMap,
            savedStateHandle = savedStateHandle,
            sortFilterControllerProvider = sortFilterControllerProvider,
            filterMedia = filterMedia,
            mediaEntryProvider = mediaEntryProvider,
        )
    }
}
