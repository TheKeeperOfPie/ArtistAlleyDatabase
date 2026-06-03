package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.lifecycle.ViewModel
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.LocalDate

@Inject
class SeriesChangelogViewModel(
    val seriesEntryCache: SeriesEntryCache,
    seriesEntryDao: SeriesEntryDao,
    private val seriesImageLoader: SeriesImageLoader,
) : ViewModel() {

    val changes = flowFromSuspend {
        seriesEntryDao.getSeriesChangelog()
    }.combine(seriesEntryCache.series, ::Pair)
        .mapLatest { (changelog, seriesTitles) ->

            changelog.sortedByDescending { it.date }
                .map {
                    SeriesChangelogScreen.DayChange(
                        date = LocalDate.parse(it.date),
                        seriesIds = it.seriesIds?.sorted().orEmpty().mapNotNull {
                            seriesTitles[it]
                        },
                    )
                }
        }
        .stateInForCompose(emptyList())

    fun seriesImage(seriesId: String) = seriesImageLoader.getSeriesImage(seriesId)

}
