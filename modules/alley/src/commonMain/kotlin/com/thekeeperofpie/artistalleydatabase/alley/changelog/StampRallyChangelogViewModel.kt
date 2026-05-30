package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

@AssistedInject
class StampRallyChangelogViewModel(
    stampRallyEntryDao: StampRallyEntryDao,
    dispatchers: CustomDispatchers,
    val seriesEntryCache: SeriesEntryCache,
    @Assisted dataYear: DataYear,
) : ViewModel() {

    internal val changes =
        flowFromSuspend { stampRallyEntryDao.getAllEntriesForChangelog(dataYear) }
            .mapLatest { allRallies ->
                stampRallyEntryDao.getChangelog(dataYear)
                    .mapNotNull {
                        val rally = allRallies[it.stampRallyId] ?: return@mapNotNull null
                        StampRallyChangelogEntry(
                            stampRallyId = it.stampRallyId,
                            date = LocalDate.parse(it.date),
                            images = AlleyImageUtils.getRallyImages(dataYear, it.images.orEmpty()),
                            rally = rally,
                        )
                    }
            }
            .mapLatest {
                it.groupBy { it.date }
                    .toList()
                    .sortedByDescending { it.first }
                    .map {
                        val (added, updated) = it.second.partition { it.images.isEmpty() }
                        StampRallyChangelogScreen.DayChange(
                            date = it.first,
                            added = added.sortedBy { it.rally.fandom },
                            updated = updated.sortedBy { it.rally.fandom },
                        )
                    }
            }
            .flowOn(dispatchers.io)
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
