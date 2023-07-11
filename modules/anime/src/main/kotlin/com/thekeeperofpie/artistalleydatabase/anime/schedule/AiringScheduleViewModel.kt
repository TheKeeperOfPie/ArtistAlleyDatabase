package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.filter
import androidx.paging.map
import com.anilist.AiringScheduleQuery
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AiringScheduleViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    private val ignoreList: AnimeMediaIgnoreList,
) : ViewModel() {

    var sort by mutableStateOf(AiringScheduleSort.POPULARITY)
    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()

    private val startDay = LocalDate.now().let {
        it.minusDays(it.dayOfWeek.value.toLong() - 1)
            .minusWeeks(1)
    }

    // Spans last week, current week, next week
    private val dayFlows = Array(21) {
        MutableStateFlow(PagingData.empty<Entry>())
    }

    init {
        repeat(21) {
            viewModelScope.launch(CustomDispatchers.IO) {
                combine(
                    snapshotFlow { sort },
                    settings.showAdult,
                    settings.showIgnored,
                    ::Triple
                ).flatMapLatest { (sort, showAdult, showIgnored) ->
                    Pager(PagingConfig(10)) {
                        AiringSchedulePagingSource(
                            aniListApi, AiringSchedulePagingSource.RefreshParams(
                                sort = sort,
                                day = startDay.plusDays(it.toLong()),
                                requestMillis = -1,
                            )
                        )
                    }
                        .flow.map {
                            it.filter { showAdult || it.media?.isAdult == false }
                                .filter {
                                    showIgnored || !(it.media?.id?.let(ignoreList::get) ?: false)
                                }
                                .map {
                                    Entry(
                                        data = it,
                                        ignored = it.media?.id?.let(ignoreList::get) ?: false
                                    )
                                }
                        }
                }
                    .cachedIn(viewModelScope)
                    .collectLatest(dayFlows[it]::emit)
            }
        }
    }

    @Composable
    fun items(index: Int) = dayFlows[index].collectAsLazyPagingItems()

    fun onLongClickEntry(entry: AnimeMediaListRow.Entry<*>) {
        val mediaId = entry.media.id.toString()
        val ignored = !entry.ignored
        ignoreList.set(mediaId, ignored)
        entry.ignored = ignored
    }

    class Entry(
        val data: AiringScheduleQuery.Data.Page.AiringSchedule,
        ignored: Boolean,
    ) {
        var ignored by mutableStateOf(ignored)
    }
}
