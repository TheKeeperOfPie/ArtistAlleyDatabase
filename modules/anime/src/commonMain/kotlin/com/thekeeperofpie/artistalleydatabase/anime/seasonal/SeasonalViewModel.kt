@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

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
import com.anilist.data.type.MediaSeason
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaGenresController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchMediaPagingSource
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.milliseconds

@Inject
class SeasonalViewModel(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    val ignoreController: IgnoreController,
    private val statusController: MediaListStatusController,
    mediaTagsController: MediaTagsController,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    featureOverrideProvider: FeatureOverrideProvider,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {

    private val destination = savedStateHandle.toDestination<AnimeDestination.Seasonal>(navigationTypeMap)

    val viewer = aniListApi.authedUser
    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)

    // TODO: Does this actually evict old pages from memory?
    private val pages = LruCache<Int, Page>(10)
    private val currentSeasonYear = AniListUtils.getCurrentSeasonYear()

    var initialPage = when (destination.type) {
        AnimeDestination.Seasonal.Type.LAST -> Int.MAX_VALUE / 2 - 1
        AnimeDestination.Seasonal.Type.THIS -> Int.MAX_VALUE / 2
        AnimeDestination.Seasonal.Type.NEXT -> Int.MAX_VALUE / 2 + 1
    }

    val sortFilterController = AnimeSortFilterController(
        sortTypeEnumClass = MediaSortOption::class,
        scope = viewModelScope,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController,
        mediaGenresController = mediaGenresController,
        mediaLicensorsController = mediaLicensorsController,
    )

    private val refresh = RefreshFlow()

    init {
        sortFilterController.initialize(
            initialParams = AnimeSortFilterController.InitialParams(
                airingDateEnabled = false,
                defaultSort = MediaSortOption.POPULARITY,
                lockSort = false,
            ),
        )
    }

    fun onRefresh() = refresh.refresh()

    @Composable
    fun items(page: Int): LazyPagingItems<MediaPreviewWithDescriptionEntry> {
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

    inner class Page(seasonYear: Pair<MediaSeason, Int>) {
        var content = MutableStateFlow(PagingData.empty<MediaPreviewWithDescriptionEntry>())

        init {
            viewModelScope.launch(CustomDispatchers.Main) {
                combine(
                    MediaUtils.mediaViewOptionIncludeDescriptionFlow { mediaViewOption },
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
                    .debounce(100.milliseconds)
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
                    .map { it.mapOnIO { MediaPreviewWithDescriptionEntry(it) } }
                    .cachedIn(viewModelScope)
                    .flatMapLatest { sortFilterController.filterMedia(it) { it.media } }
                    .applyMediaStatusChanges(
                        statusController = statusController,
                        ignoreController = ignoreController,
                        mediaFilteringData = settings.mediaFilteringData(false),
                        mediaFilterable = { it.mediaFilterable },
                        copy = { copy(mediaFilterable = it) },
                    )
                    .cachedIn(viewModelScope)
                    .flowOn(CustomDispatchers.IO)
                    .collectLatest(content::emit)
            }
        }
    }
}
