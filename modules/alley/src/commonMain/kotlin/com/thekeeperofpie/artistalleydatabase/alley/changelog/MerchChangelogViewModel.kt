package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.lifecycle.ViewModel
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

@Inject
class MerchChangelogViewModel(merchEntryDao: MerchEntryDao) : ViewModel() {

    val changes = flowFromSuspend {
        merchEntryDao.getMerchChangelog()
            .sortedByDescending { it.date }
            .map {
                MerchChangelogScreen.DayChange(
                    date = LocalDate.parse(it.date),
                    merchIds = it.merchIds?.sorted().orEmpty(),
                )
            }
    }.stateInForCompose(emptyList())

}
