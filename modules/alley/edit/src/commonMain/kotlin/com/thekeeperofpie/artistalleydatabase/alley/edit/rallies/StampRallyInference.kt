package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallySummary
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.mapLatest

@SingleIn(AppScope::class)
@Inject
class StampRallyInference(
    private val rallyCache: StampRallyCache,
) {

    fun inferRallies(dataYear: DataYear, input: Input) =
        rallyCache.stampRallies(dataYear)
            .mapLatest { rallies ->
                val byTable = rallies.filter { it.tables.intersect(input.tables).isNotEmpty() }
                    .groupBy { it.tables.intersect(input.tables) }

                val bySeries = rallies.filter { it.series.intersect(input.seriesIds).isNotEmpty() }
                    .groupBy { it.series.intersect(input.seriesIds) }

                val both = byTable.values.flatten().intersect(bySeries.values.flatten().toSet())
                val byBoth = both.groupBy {
                    Output.TablesAndSeries(
                        tables = it.tables.intersect(input.tables),
                        seriesIds = it.series.intersect(input.seriesIds),
                    )
                }

                Output(
                    byTables = byTable.mapValues { it.value - both }.filterValues { it.isNotEmpty() },
                    bySeries = bySeries.mapValues { it.value - both }.filterValues { it.isNotEmpty() },
                    byBoth = byBoth,
                )
            }

    data class Input(
        val tables: Set<String>,
        val seriesIds: Set<String>,
    )

    data class Output(
        val byTables: Map<Set<String>, List<StampRallySummary>>,
        val bySeries: Map<Set<String>, List<StampRallySummary>>,
        val byBoth: Map<TablesAndSeries, List<StampRallySummary>>,
    ) {
        data class TablesAndSeries(
            val tables: Set<String>,
            val seriesIds: Set<String>,
        )
    }
}
