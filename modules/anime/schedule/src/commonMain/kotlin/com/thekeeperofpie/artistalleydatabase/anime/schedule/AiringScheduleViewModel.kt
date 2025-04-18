package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.data.AiringScheduleQuery
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.type.AiringSort
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.PagingUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AiringScheduleViewModel<MediaEntry : Any>(
    private val aniListApi: AuthedAniListApi,
    private val settings: MediaDataSettings,
    private val statusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    featureOverrideProvider: FeatureOverrideProvider,
    @Assisted private val airingScheduleSortFilterViewModel: AiringScheduleSortFilterViewModel,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>,
) : ViewModel() {

    val viewer = aniListApi.authedUser
    var refresh = RefreshFlow()

    private val startDay =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.let {
            it.minus(it.dayOfWeek.value.toLong() - 1, DateTimeUnit.DAY)
                .minus(1, DateTimeUnit.WEEK)
        }

    // Spans last week, current week, next week
    private val dayFlows = Array(21) {
        MutableStateFlow(PagingUtils.loading<AiringScheduleScreen.Entry<MediaEntry>>())
    }
    private val initialized = Array(21) { false }

    private fun initialize(index: Int) {
        initialized[index] = true
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                airingScheduleSortFilterViewModel.state.filterParams,
                refresh.updates,
                ::Pair
            )
                .flatMapLatest { (filterParams) -> buildPagingData(index, filterParams) }
                .map {
                    it.mapOnIO {
                        AiringScheduleScreen.Entry(
                            data = it,
                            media = it.media?.let(mediaEntryProvider::mediaEntry)
                        )
                    }
                }
                .cachedIn(viewModelScope)
                .applyMediaStatusChanges(
                    statusController = statusController,
                    ignoreController = ignoreController,
                    mediaFilteringData = settings.mediaFilteringData(false),
                    mediaFilterable = { it.media?.let(mediaEntryProvider::mediaFilterable) },
                    copy = { mediaFilterable ->
                        copy(media = media?.let { media ->
                            mediaEntryProvider.copyMediaEntry(media, mediaFilterable)
                        })
                    },
                )
                .cachedIn(viewModelScope)
                .collectLatest(dayFlows[index]::emit)
        }
    }

    private fun buildPagingData(
        index: Int,
        filterParams: AiringScheduleSortFilterViewModel.FilterParams,
    ): Flow<PagingData<AiringScheduleQuery.Data.Page.AiringSchedule>> {
        val sort = filterParams.sort
        val date = startDay.plus(index.toLong(), DateTimeUnit.DAY)
        val timeZone = TimeZone.currentSystemDefault()
        val startTime = date.atStartOfDayIn(timeZone).epochSeconds - 1
        val endTime = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).epochSeconds

        return if (sort == AiringScheduleSortOption.POPULARITY) {
            flow {
                emit(PagingData.empty())

                var currentPage = 1
                var hasNextPage = true
                val list = mutableListOf<AiringScheduleQuery.Data.Page.AiringSchedule>()
                val loadStates = try {
                    while (hasNextPage) {
                        val result = aniListApi.airingSchedule(
                            startTime = startTime,
                            endTime = endTime,
                            sort = AiringSort.ID_DESC,
                            perPage = 25,
                            page = currentPage++,
                        )
                        hasNextPage = result.page?.pageInfo?.hasNextPage ?: false
                        list += result.page?.airingSchedules?.filterNotNull().orEmpty()
                        delay(500.milliseconds)
                    }
                    LoadStates(
                        refresh = LoadState.NotLoading(true),
                        prepend = LoadState.NotLoading(true),
                        append = LoadState.NotLoading(true),
                    )
                } catch (e: Throwable) {
                    LoadStates(
                        refresh = LoadState.Error(e),
                        prepend = LoadState.NotLoading(true),
                        append = LoadState.NotLoading(true),
                    )
                }

                val comparator =
                    compareByDescending<AiringScheduleQuery.Data.Page.AiringSchedule, Int?>(
                        nullsLast()
                    ) {
                        it.media?.popularity
                    }.thenBy(nullsLast()) { it.airingAt }

                emit(
                    PagingData.from(
                        list.sortedWith(
                            if (filterParams.sortAscending) comparator.reversed() else comparator
                        ),
                        loadStates,
                    )
                )
            }
        } else {
            AniListPager {
                val result = aniListApi.airingSchedule(
                    startTime = startTime,
                    endTime = endTime,
                    sort = sort.toApiValue(filterParams.sortAscending),
                    perPage = 10,
                    page = it,
                )
                result.page?.pageInfo to result.page?.airingSchedules?.filterNotNull().orEmpty()
            }
        }
    }

    @Composable
    fun items(index: Int): LazyPagingItems<AiringScheduleScreen.Entry<MediaEntry>> {
        if (!initialized[index]) {
            initialize(index)
        }
        return dayFlows[index].collectAsLazyPagingItems()
    }

    fun refresh() = refresh.refresh()

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val settings: MediaDataSettings,
        private val statusController: MediaListStatusController,
        private val ignoreController: IgnoreController,
        private val featureOverrideProvider: FeatureOverrideProvider,
        @Assisted private val airingScheduleSortFilterViewModel: AiringScheduleSortFilterViewModel,
    ) {
        fun <MediaEntry : Any> create(
            mediaEntryProvider: MediaEntryProvider<MediaPreview, MediaEntry>,
        ) = AiringScheduleViewModel(
            aniListApi = aniListApi,
            settings = settings,
            statusController = statusController,
            ignoreController = ignoreController,
            featureOverrideProvider = featureOverrideProvider,
            airingScheduleSortFilterViewModel = airingScheduleSortFilterViewModel,
            mediaEntryProvider = mediaEntryProvider,
        )
    }
}
