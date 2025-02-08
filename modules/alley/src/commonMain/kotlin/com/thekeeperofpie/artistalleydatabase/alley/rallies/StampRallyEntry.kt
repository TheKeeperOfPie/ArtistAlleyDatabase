package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear

data class StampRallyEntry(
    val year: DataYear,
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
