package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.AiringScheduleQuery
import com.anilist.type.AiringSort
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.selectedOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.LazyPagingItems
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
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class AiringScheduleViewModel(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    private val statusController: MediaListStatusController,
    private val ignoreController: IgnoreController,
    featureOverrideProvider: FeatureOverrideProvider,
) : ViewModel() {

    val viewer = aniListApi.authedUser
    var sortFilterController =
        AiringScheduleSortFilterController(viewModelScope, settings, featureOverrideProvider)
    var refresh = MutableStateFlow(-1)

    private val startDay = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.let {
        it.minus(it.dayOfWeek.value.toLong() - 1, DateTimeUnit.DAY)
            .minus(1, DateTimeUnit.WEEK)
    }

    // Spans last week, current week, next week
    private val dayFlows = Array(21) {
        MutableStateFlow(PagingData.empty<Entry>())
    }
    private val initialized = Array(21) { false }

    private fun initialize(index: Int) {
        initialized[index] = true
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                refresh,
                sortFilterController.filterParams,
                ::Pair
            )
                .flatMapLatest { (_, filterParams) -> buildPagingData(index, filterParams) }
                .map { it.mapOnIO { Entry(data = it) } }
                .cachedIn(viewModelScope)
                .applyMediaStatusChanges(
                    statusController = statusController,
                    ignoreController = ignoreController,
                    settings = settings,
                    media = { it.data.media },
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
                .cachedIn(viewModelScope)
                .collectLatest(dayFlows[index]::emit)
        }
    }

    private fun buildPagingData(
        index: Int,
        filterParams: AiringScheduleSortFilterController.FilterParams,
    ): Flow<PagingData<AiringScheduleQuery.Data.Page.AiringSchedule>> {
        val sort = filterParams.sort.selectedOption(AiringScheduleSortOption.POPULARITY)
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
    fun items(index: Int): LazyPagingItems<Entry> {
        if (!initialized[index]) {
            initialize(index)
        }
        return dayFlows[index].collectAsLazyPagingItems()
    }

    data class Entry(
        val data: AiringScheduleQuery.Data.Page.AiringSchedule,
        override val mediaListStatus: MediaListStatus? = data.media?.mediaListEntry?.status,
        override val progress: Int? = data.media?.mediaListEntry?.progress,
        override val progressVolumes: Int? = data.media?.mediaListEntry?.progressVolumes,
        override val scoreRaw: Double? = data.media?.mediaListEntry?.score,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
    ) : MediaStatusAware {
        val entry = data.media?.let {
            MediaPreviewEntry(
                media = it,
                mediaListStatus = mediaListStatus,
                progress = progress,
                progressVolumes = progressVolumes,
                scoreRaw = scoreRaw,
                ignored = ignored,
                showLessImportantTags = showLessImportantTags,
                showSpoilerTags = showSpoilerTags,
            )
        }
    }
}