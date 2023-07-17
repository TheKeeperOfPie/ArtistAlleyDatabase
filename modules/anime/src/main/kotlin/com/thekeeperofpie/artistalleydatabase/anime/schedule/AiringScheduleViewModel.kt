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
import androidx.paging.map
import com.anilist.AiringScheduleQuery
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaStatusChanges
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
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
    private val statusController: MediaListStatusController,
) : ViewModel() {

    val viewer = aniListApi.authedUser
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
                snapshotFlow { sort }
                    .flowOn(CustomDispatchers.Main)
                    .flatMapLatest { sort ->
                        Pager(PagingConfig(10)) {
                            AiringSchedulePagingSource(
                                aniListApi, AiringSchedulePagingSource.RefreshParams(
                                    sort = sort,
                                    day = startDay.plusDays(it.toLong()),
                                    requestMillis = -1,
                                )
                            )
                        }.flow
                    }
                    .map { it.map { Entry(data = it) } }
                    .cachedIn(viewModelScope)
                    .applyMediaStatusChanges(
                        statusController = statusController,
                        ignoreList = ignoreList,
                        settings = settings,
                        media = { it.data.media },
                        copy = { mediaListStatus, progress, progressVolumes, ignored ->
                            copy(
                                mediaListStatus = mediaListStatus,
                                progress = progress,
                                progressVolumes = progressVolumes,
                                ignored = ignored,
                            )
                        },
                    )
                    .collectLatest(dayFlows[it]::emit)
            }
        }
    }

    @Composable
    fun items(index: Int) = dayFlows[index].collectAsLazyPagingItems()

    fun onLongClickEntry(entry: AnimeMediaListRow.Entry<*>) =
        ignoreList.toggle(entry.media.id.toString())

    data class Entry(
        val data: AiringScheduleQuery.Data.Page.AiringSchedule,
        override val mediaListStatus: MediaListStatus? = data.media?.mediaListEntry?.status,
        override val progress: Int? = null,
        override val progressVolumes: Int? = null,
        override val ignored: Boolean = false,
    ) : MediaStatusAware {
        val entry = data.media?.let {
            AnimeMediaListRow.Entry(
                media = it,
                mediaListStatus = mediaListStatus,
                progress = progress,
                progressVolumes = progressVolumes,
                ignored = ignored
            )
        }
    }
}
