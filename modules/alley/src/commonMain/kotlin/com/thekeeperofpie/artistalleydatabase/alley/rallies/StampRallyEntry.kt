package com.thekeeperofpie.artistalleydatabase.alley.rallies

import kotlinx.serialization.Serializable

@Serializable
expect class StampRallyEntry {
    val id: String
    val fandom: String
    val hostTable: String
    val tables: List<String>
    val links: List<String>
    val tableMin: Int?
    val totalCost: Int?
    val prizeLimit: Int?
    val favorite: Boolean
    val ignored: Boolean
    val notes: String?
    // Used for random ordering while maintaining a stable key
    val counter: Int

    constructor(
        id: String,
        fandom: String,
        hostTable: String,
        tables: List<String>,
        links: List<String> = emptyList(),
        tableMin: Int? = null,
        totalCost: Int? = null,
        prizeLimit: Int? = null,
        favorite: Boolean = false,
        ignored: Boolean = false,
        notes: String? = null,
        counter: Int = 1,
    )

    fun copy(
        id: String = this.id,
        fandom: String = this.fandom,
        hostTable: String = this.hostTable,
        tables: List<String> = this.tables,
        links: List<String> = this.links,
        tableMin: Int? = this.tableMin,
        totalCost: Int? = this.totalCost,
        prizeLimit: Int? = this.prizeLimit,
        favorite: Boolean = this.favorite,
        ignored: Boolean = this.ignored,
        notes: String? = this.notes,
    ) : StampRallyEntry
}
