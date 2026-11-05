package com.thekeeperofpie.artistalleydatabase.alley.series

import androidx.appstate.transform.transform
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.thekeeperofpie.artistalleydatabase.alley.GetSeriesTitles
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class SeriesEntryCache(
    scope: ApplicationScope,
    private val seriesEntryDao: SeriesEntryDao,
) {
    val series by transform(emptyMap<String, GetSeriesTitles>(), scope) {
        produceState(emptyMap()) {
            value = seriesEntryDao.getSeriesTitles().associateBy { it.id }
        }.value
    }
}
