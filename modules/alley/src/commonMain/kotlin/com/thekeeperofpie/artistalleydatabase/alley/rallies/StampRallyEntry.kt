package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

data class StampRallyEntry(
    val year: DataYear,
    val id: String,
    val fandom: String,
    val hostTable: String,
    val tables: List<String>,
    val links: List<String>,
    val tableMin: Long?,
    val totalCost: Long?,
    val prize: String?,
    val prizeLimit: Long?,
    val series: List<String>,
    val notes: String?,
    val counter: Long,
    val confirmed: Boolean,
)
