package com.thekeeperofpie.artistalleydatabase.alley.merch

import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@SingleIn(AppScope::class)
@Inject
class MerchCache(
    private val appScope: ApplicationScope,
    private val merchEntryDao: MerchEntryDao,
    private val dispatchers: CustomDispatchers,
) {
    val flows = DataYear.entries.associateWith(::merch)

    fun merchTags(dataYear: DataYear) = flows[dataYear]!!

    private fun merch(dataYear: DataYear) = flowFromSuspend { merchEntryDao.getMerchEntries(dataYear) }
        .mapLatest(::MerchTagData)
        .flowOn(dispatchers.io)
        .stateIn(appScope, SharingStarted.Lazily, MerchTagData(emptyList()))
}
