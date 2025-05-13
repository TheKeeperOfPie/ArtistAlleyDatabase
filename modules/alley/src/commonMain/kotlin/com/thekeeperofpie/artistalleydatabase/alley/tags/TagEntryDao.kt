package com.thekeeperofpie.artistalleydatabase.alley.tags

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.MerchQueries
import com.thekeeperofpie.artistalleydatabase.alley.SeriesQueries
import com.thekeeperofpie.artistalleydatabase.alley.tags.map.TagMapQuery
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class TagEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val seriesDao: suspend () -> SeriesQueries = { database().seriesQueries },
    private val merchDao: suspend () -> MerchQueries = { database().merchQueries },
) {

    suspend fun getBooths(year: DataYear, tagMapQuery: TagMapQuery): Set<String> =
        if (year == DataYear.YEAR_2023) {
            emptySet()
        } else {
            val seriesDao = seriesDao()
            val merchDao = merchDao()
            val seriesId = tagMapQuery.series
            if (seriesId != null) {
                if (tagMapQuery.showOnlyConfirmedTags) {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> seriesDao.getBoothsBySeriesIdConfirmed2024(seriesId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> seriesDao.getBoothsBySeriesIdConfirmed2025(seriesId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                } else {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> seriesDao.getBoothsBySeriesId2024(seriesId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> seriesDao.getBoothsBySeriesId2025(seriesId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                }
            } else {
                val merchId = tagMapQuery.merch!!
                if (tagMapQuery.showOnlyConfirmedTags) {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> merchDao.getBoothsByMerchIdConfirmed2024(merchId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> merchDao.getBoothsByMerchIdConfirmed2025(merchId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                } else {
                    when (year) {
                        DataYear.YEAR_2023 -> emptyList()
                        DataYear.YEAR_2024 -> merchDao.getBoothsByMerchId2024(merchId)
                            .awaitAsList()
                        DataYear.YEAR_2025 -> merchDao.getBoothsByMerchId2025(merchId)
                            .awaitAsList()
                            .map { it.booth }
                    }
                }
            }.filterNotNull().toSet()
        }
}
