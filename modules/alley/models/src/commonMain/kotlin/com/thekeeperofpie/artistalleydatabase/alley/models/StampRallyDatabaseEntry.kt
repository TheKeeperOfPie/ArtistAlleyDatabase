package com.thekeeperofpie.artistalleydatabase.alley.models

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
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
    val links: List<String>,
    val tableMin: TableMin?,
    val totalCost: Long?,
    val prize: String?,
    val prizeLimit: Long?,
    val series: List<String>,
    val merch: List<String>,
    val notes: String?,
    val images: List<CatalogImage>,
    val counter: Long,
    val confirmed: Boolean,
    val editorNotes: String?,
    val lastEditor: String?,
    val lastEditTime: Instant?,
) {
    companion object {

        // Need to ignore metadata for equality
        fun hasChanged(before: StampRallyDatabaseEntry?, after: StampRallyDatabaseEntry) =
            before?.copy(counter = 0, lastEditTime = null, lastEditor = null) !=
                    after.copy(counter = 0, lastEditTime = null, lastEditor = null)
    }
}

fun StampRallyDatabaseEntry.toStampRallySummary() = StampRallySummary(
    id = id,
    fandom = fandom,
    hostTable = hostTable,
    tables = tables,
    series = series,
)
