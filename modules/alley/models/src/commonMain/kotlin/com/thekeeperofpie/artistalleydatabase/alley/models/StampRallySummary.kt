package com.thekeeperofpie.artistalleydatabase.alley.models

import kotlinx.serialization.Serializable

@Serializable
data class StampRallySummary(
    val id: String,
    val fandom: String,
    val hostTable: String,
    val tables: List<String>,
    val series: List<String>,
)
