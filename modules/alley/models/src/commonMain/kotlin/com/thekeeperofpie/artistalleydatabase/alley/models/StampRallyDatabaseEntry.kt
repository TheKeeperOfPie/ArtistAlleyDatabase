package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.TableMin
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class StampRallyDatabaseEntry(
    val year: DataYear,
    val id: String,
    val fandom: String,
    val hostTable: String,
    val tables: List<String>,
    val startTables: Set<String>,
    val endTables: Set<String>,
    val links: List<String>,
    val tableMin: TableMin?,
    val totalCost: Long?,
    val prize: String?,
    val prizeLimit: Long?,
    val prizeMerch: List<String>,
    val series: List<String>,
    val merch: List<String>,
    val notes: String?,
    val images: List<DatabaseImage>,
    val confirmed: Boolean,
    val editorNotes: String?,
    val lastEditor: String?,
    val lastEditTime: Instant?,
) {
    companion object {

        const val MAX_STAMP_RALLIES = 10

        // Need to ignore metadata for equality
        fun hasChanged(before: StampRallyDatabaseEntry?, after: StampRallyDatabaseEntry) =
            before?.copy(
                images = before.images.map {
                    it.copy(
                        width = null,
                        height = null,
                        color = null,
                    )
                },
                confirmed = false,
                lastEditTime = null,
                lastEditor = null,
            ) != after.copy(
                images = after.images.map {
                    it.copy(
                        width = null,
                        height = null,
                        color = null,
                    )
                },
                confirmed = false,
                lastEditTime = null,
                lastEditor = null,
            )

        fun empty(year: DataYear, id: String) = StampRallyDatabaseEntry(
            year = year,
            id = id,
            fandom = "",
            hostTable = "",
            tables = emptyList(),
            startTables = emptySet(),
            endTables = emptySet(),
            links = emptyList(),
            tableMin = null,
            totalCost = null,
            prize = null,
            prizeLimit = null,
            prizeMerch = emptyList(),
            series = emptyList(),
            merch = emptyList(),
            notes = null,
            images = emptyList(),
            confirmed = false,
            editorNotes = null,
            lastEditor = null,
            lastEditTime = null,
        )
    }
}

fun StampRallyDatabaseEntry.toStampRallySummary() = StampRallySummary(
    id = id,
    fandom = fandom,
    hostTable = hostTable,
    tables = tables,
    series = series,
)
