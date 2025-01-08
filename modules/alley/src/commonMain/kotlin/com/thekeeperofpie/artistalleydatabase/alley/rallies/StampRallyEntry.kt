package com.thekeeperofpie.artistalleydatabase.alley.rallies

import kotlinx.serialization.Serializable

@Serializable
data class StampRallyEntry(
    val id: String,
    val fandom: String,
    val hostTable: String,
    val tables: List<String>,
    val links: List<String> = emptyList(),
    val tableMin: Int? = null,
    val totalCost: Int? = null,
    val prizeLimit: Int? = null,
    val favorite: Boolean = false,
    val ignored: Boolean = false,
    val notes: String? = null,
    // Used for random ordering while maintaining a stable key
    val counter: Int = 1,
)
