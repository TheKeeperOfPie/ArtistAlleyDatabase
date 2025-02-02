package com.thekeeperofpie.artistalleydatabase.alley.rallies

data class StampRallyEntry(
    val id: String,
    val fandom: String,
    val hostTable: String,
    val tables: List<String>,
    val links: List<String>,
    val tableMin: Long?,
    val totalCost: Long?,
    val prizeLimit: Long?,
    val notes: String?,
    val counter: Long,
)
