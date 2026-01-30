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
    val notes: String?,
    val images: List<CatalogImage>,
    val confirmed: Boolean,
    val editorNotes: String?,
    val lastEditor: String?,
    val lastEditTime: Instant?,
)
