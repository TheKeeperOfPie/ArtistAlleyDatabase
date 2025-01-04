package com.thekeeperofpie.artistalleydatabase.alley.rallies

import kotlinx.serialization.Serializable

@Serializable
actual data class StampRallyEntry actual constructor(
    actual val id: String,
    actual val fandom: String,
    actual val hostTable: String,
    actual val tables: List<String>,
    actual val links: List<String>,
    actual val tableMin: Int?,
    actual val totalCost: Int?,
    actual val prizeLimit: Int?,
    actual val favorite: Boolean,
    actual val ignored: Boolean,
    actual val notes: String?,
    // Used fo random ordering while maintaining a stable key
    actual val counter: Int,
) {
    actual fun copy(
        id: String,
        fandom: String,
        hostTable: String,
        tables: List<String>,
        links: List<String>,
        tableMin: Int?,
        totalCost: Int?,
        prizeLimit: Int?,
        favorite: Boolean,
        ignored: Boolean,
        notes: String?,
    ) = copy(
        id = id,
        fandom = fandom,
        hostTable = hostTable,
        tables = tables,
        links = links,
        tableMin = tableMin,
        totalCost = totalCost,
        prizeLimit = prizeLimit,
        favorite = favorite,
        ignored = ignored,
        notes = notes,
        counter = this.counter,
    )
}
