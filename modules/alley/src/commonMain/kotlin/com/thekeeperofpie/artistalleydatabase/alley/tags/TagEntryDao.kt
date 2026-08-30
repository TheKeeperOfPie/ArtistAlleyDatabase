package com.thekeeperofpie.artistalleydatabase.alley.tags

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.MerchQueries
import com.thekeeperofpie.artistalleydatabase.alley.SeriesQueries
import com.thekeeperofpie.artistalleydatabase.alley.database.ArtistAlleyDatabase
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapQuery
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TagYearFlag
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi

@SingleIn(AppScope::class)
class TagEntryDao(
    private val database: suspend () -> AlleySqlDatabase,
    private val seriesDao: suspend () -> SeriesQueries = { database().seriesQueries },
    private val merchDao: suspend () -> MerchQueries = { database().merchQueries },
) {
    @Inject
    constructor(database: ArtistAlleyDatabase) : this(database = database::database)

    suspend fun getBooths(year: DataYear, tagMapQuery: TagMapQuery): Set<String> {
        val seriesDao = seriesDao()
        val merchDao = merchDao()
        val seriesId = tagMapQuery.series
        val yearFlag = TagYearFlag.getFlag(
            year = year,
            confirmed = tagMapQuery.showOnlyConfirmedTags,
        )
        return if (seriesId != null) {
            seriesDao.getBoothsBySeriesId(year, seriesId, yearFlag)
                .awaitAsList()
                .map { it.booth }
        } else {
            val merchId = tagMapQuery.merch!!
            merchDao.getBoothsByMerchId(year, merchId, yearFlag)
                .awaitAsList()
                .map { it.booth }
        }.filterNotNull().toSet()
    }
}
