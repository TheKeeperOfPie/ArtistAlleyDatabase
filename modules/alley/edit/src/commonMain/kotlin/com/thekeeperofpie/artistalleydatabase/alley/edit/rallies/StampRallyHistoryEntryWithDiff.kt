package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.ui.util.fastForEachReversed
import com.thekeeperofpie.artistalleydatabase.alley.models.HistoryListDiff
import com.thekeeperofpie.artistalleydatabase.alley.models.StampRallyHistoryEntry

data class StampRallyHistoryEntryWithDiff(
    val entry: StampRallyHistoryEntry,
    val tablesDiff: HistoryListDiff?,
    val linksDiff: HistoryListDiff?,
    val seriesDiff: HistoryListDiff?,
    val merchDiff: HistoryListDiff?,
) {
    companion object {
        fun calculateDiffs(entries: List<StampRallyHistoryEntry>): List<StampRallyHistoryEntryWithDiff> {
            val oldestEntry = entries.lastOrNull()
            var lastTables = oldestEntry?.tables.orEmpty()
            var lastLinks = oldestEntry?.links.orEmpty()
            var lastSeries = oldestEntry?.series.orEmpty()
            var lastMerch = oldestEntry?.merch.orEmpty()
            val results = mutableListOf<StampRallyHistoryEntryWithDiff>()
            entries.fastForEachReversed {
                val tablesDiff = HistoryListDiff.diffList(lastTables, it.tables)
                val linksDiff = HistoryListDiff.diffList(lastLinks, it.links)
                val seriesDiff = HistoryListDiff.diffList(lastSeries, it.series)
                val merchDiff = HistoryListDiff.diffList(lastMerch, it.merch)
                results += StampRallyHistoryEntryWithDiff(
                    entry = it,
                    tablesDiff = tablesDiff,
                    linksDiff = linksDiff,
                    seriesDiff = seriesDiff,
                    merchDiff = merchDiff,
                )

                lastTables = it.tables ?: lastTables
                lastLinks = it.links ?: lastLinks
                lastSeries = it.series ?: lastSeries
                lastMerch = it.merch ?: lastMerch
            }

            // Reverse again re-order reverse chronologically
            return results.reversed()
        }
    }
}
