package com.thekeeperofpie.artistalleydatabase.alley.edit.tags

import com.thekeeperofpie.artistalleydatabase.alley.data.toMerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.data.toSeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class FormTagAutocomplete(
    applicationScope: ApplicationScope,
    private val dispatchers: CustomDispatchers,
    seriesEntryDao: SeriesEntryDao,
    merchEntryDao: MerchEntryDao,
) : TagAutocomplete(
    applicationScope = applicationScope,
    dispatchers = dispatchers,
    loadSeries = { seriesEntryDao.getSeries().map { it.toSeriesInfo() }.associateBy { it.id } },
    loadMerch = { merchEntryDao.getMerch().map { it.toMerchInfo() }.associateBy { it.name } },
)
