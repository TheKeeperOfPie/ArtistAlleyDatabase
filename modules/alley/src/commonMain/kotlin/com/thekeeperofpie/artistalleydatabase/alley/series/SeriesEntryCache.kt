package com.thekeeperofpie.artistalleydatabase.alley.series

import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.data.toSeriesInfo
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@SingleIn(AppScope::class)
@Inject
class SeriesEntryCache(
    scope: ApplicationScope,
    private val seriesEntryDao: SeriesEntryDao,
) {
    val series = flowFromSuspend {
        seriesEntryDao.getSeriesTitles().associateBy { it.id }
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())
}
