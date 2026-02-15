package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class StampRallyHistoryEntry(
    val fandom: String?,
    val tables: List<String>?,
    val links: List<String>?,
    val tableMin: TableMin?,
    val totalCost: Long?,
    val prize: String?,
    val prizeLimit: Long?,
    val series: List<String>?,
    val merch: List<String>?,
    val notes: String?,
    val images: List<CatalogImage>?,
    val editorNotes: String?,
    val lastEditor: String?,
    val timestamp: Instant,
    val formTimestamp: Instant?,
) {
    companion object {
        fun create(
            before: StampRallyDatabaseEntry?,
            after: StampRallyDatabaseEntry,
            formTimestamp: Instant?,
        ) = StampRallyHistoryEntry(
            fandom = after.fandom.takeIf { it != before?.fandom },
            tables = after.tables.takeIf { it != before?.tables },
            links = after.links.takeIf { it != before?.links },
            tableMin = after.tableMin.takeIf { it != before?.tableMin },
            totalCost = after.totalCost.takeIf { it != before?.totalCost },
            prize = after.prize.takeIf { it != before?.prize },
            prizeLimit = after.prizeLimit.takeIf { it != before?.prizeLimit },
            series = after.series.takeIf { it != before?.series },
            merch = after.merch.takeIf { it != before?.merch },
            notes = after.notes.takeIf { it != before?.notes },
            images = after.images.takeIf { it != before?.images },
            editorNotes = after.editorNotes.takeIf { it != before?.editorNotes },
            lastEditor = after.lastEditor,
            timestamp = after.lastEditTime ?: Clock.System.now(),
            formTimestamp = formTimestamp,
        )

        fun rebuild(
            dataYear: DataYear,
            stampRallyId: String,
            list: List<StampRallyHistoryEntry>,
        ): StampRallyDatabaseEntry {
            var fandom: String? = null
            var tables: List<String>? = null
            var links: List<String>? = null
            var tableMin: TableMin? = null
            var totalCost: Long? = null
            var prize: String? = null
            var prizeLimit: Long? = null
            var series: List<String>? = null
            var merch: List<String>? = null
            var notes: String? = null
            var images: List<CatalogImage>? = null
            var editorNotes: String? = null
            var lastEditor: String? = null

            list.forEach {
                fandom = fandom ?: it.fandom
                tables = tables ?: it.tables
                links = links ?: it.links
                tableMin = tableMin ?: it.tableMin
                totalCost = totalCost ?: it.totalCost
                prize = prize ?: it.prize
                prizeLimit = prizeLimit ?: it.prizeLimit
                series = series ?: it.series
                merch = merch ?: it.merch
                notes = notes ?: it.notes
                images = images ?: it.images
                editorNotes = editorNotes ?: it.editorNotes
                lastEditor = lastEditor ?: it.lastEditor
            }

            return StampRallyDatabaseEntry(
                year = dataYear,
                id = stampRallyId,
                fandom = fandom.orEmpty(),
                hostTable = tables?.firstOrNull().orEmpty(),
                tables = tables.orEmpty(),
                links = links.orEmpty(),
                tableMin = tableMin,
                totalCost = totalCost,
                prize = prize,
                prizeLimit = prizeLimit,
                series = series.orEmpty(),
                merch = merch.orEmpty(),
                notes = notes,
                images = images.orEmpty(),
                counter = 0L,
                confirmed = !images.isNullOrEmpty() || !links.isNullOrEmpty(),
                editorNotes = editorNotes,
                lastEditor = lastEditor,
                lastEditTime = Clock.System.now(),
            )
        }

        fun applyOver(initial: StampRallyDatabaseEntry, entry: StampRallyHistoryEntry) =
            initial.copy(
                fandom = entry.fandom ?: initial.fandom,
                tables = entry.tables ?: initial.tables,
                links = entry.links ?: initial.links,
                tableMin = entry.tableMin ?: initial.tableMin,
                totalCost = entry.totalCost ?: initial.totalCost,
                prize = entry.prize ?: initial.prize,
                prizeLimit = entry.prizeLimit ?: initial.prizeLimit,
                series = entry.series ?: initial.series,
                notes = entry.notes ?: initial.notes,
                images = entry.images ?: initial.images,
                editorNotes = entry.editorNotes ?: initial.editorNotes,
                lastEditor = null,
                lastEditTime = Clock.System.now(),
            )
    }
}
