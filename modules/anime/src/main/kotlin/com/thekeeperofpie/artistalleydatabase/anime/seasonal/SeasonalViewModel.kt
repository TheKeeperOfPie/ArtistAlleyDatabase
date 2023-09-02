@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.seasonal

import android.os.SystemClock
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
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.anilist.type.MediaSeason
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges2
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AnimeSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaGenresController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaLicensorsController
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaTagsController
import com.thekeeperofpie.artistalleydatabase.anime.search.AnimeSearchMediaPagingSource
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SeasonalViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    val ignoreController: IgnoreController,
    private val statusController: MediaListStatusController,
    mediaTagsController: MediaTagsController,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    featureOverrideProvider: FeatureOverrideProvider,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val viewer = aniListApi.authedUser
    var mediaViewOption by mutableStateOf(settings.mediaViewOption.value)

    // TODO: Does this actually evict old pages from memory?
    private val pages = LruCache<Int, Page>(10)
    private val currentSeasonYear = AniListUtils.getCurrentSeasonYear()

    private val type = savedStateHandle.get<String?>("type")?.let {
        try {
            Type.valueOf(it)
        } catch (ignored: Throwable) {
            null
        }
    } ?: Type.THIS

    var initialPage = when (type) {
        Type.LAST -> Int.MAX_VALUE / 2 - 1
        Type.THIS -> Int.MAX_VALUE / 2
        Type.NEXT -> Int.MAX_VALUE / 2 + 1
    }


    val sortFilterController = AnimeSortFilterController(
        sortTypeEnumClass = MediaSortOption::class,
        aniListApi = aniListApi,
        settings = settings,
        featureOverrideProvider = featureOverrideProvider,
        mediaTagsController = mediaTagsController,
        mediaGenresController = mediaGenresController,
        mediaLicensorsController = mediaLicensorsController,
        userScoreEnabled = false,
    )

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    init {
        sortFilterController.initialize(
            viewModel = this,
            refreshUptimeMillis = refreshUptimeMillis,
            initialParams = AnimeSortFilterController.InitialParams(
                airingDateEnabled = false,
                defaultSort = MediaSortOption.POPULARITY,
                lockSort =  false,
            ),
        )
    }

    fun onRefresh() = refreshUptimeMillis.update { SystemClock.uptimeMillis() }

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

    enum class Type {
        LAST,
        THIS,
        NEXT,
    }

    inner class Page(seasonYear: Pair<MediaSeason, Int>) {
        var content = MutableStateFlow(PagingData.empty<MediaPreviewWithDescriptionEntry>())

        init {
            viewModelScope.launch(CustomDispatchers.Main) {
                combine(
                    MediaUtils.mediaViewOptionIncludeDescriptionFlow { mediaViewOption },
                    refreshUptimeMillis,
                    sortFilterController.filterParams(),
                ) { includeDescription, requestMillis, filterParams ->
                    AnimeSearchMediaPagingSource.RefreshParams(
                        query = "",
                        includeDescription = includeDescription,
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
                    .map { it.mapOnIO { MediaPreviewWithDescriptionEntry(it) } }
                    .cachedIn(viewModelScope)
                    .applyMediaStatusChanges2(
                        statusController = statusController,
                        ignoreController = ignoreController,
                        settings = settings,
                    )
                    .flatMapLatest { sortFilterController.filterMedia(it) { it.media } }
                    .cachedIn(viewModelScope)
                    .flowOn(CustomDispatchers.IO)
                    .collectLatest(content::emit)
            }
        }
    }
}
