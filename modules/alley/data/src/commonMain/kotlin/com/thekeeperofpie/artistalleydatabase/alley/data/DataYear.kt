package com.thekeeperofpie.artistalleydatabase.alley.data

import kotlinx.serialization.Serializable

// Duplicated into build-logic for SQLDelght codegen
@Serializable
enum class DataYear(val serializedName: String, val year: Int) {
    YEAR_2023("AX2023", 2023), YEAR_2024("AX2024", 2024), YEAR_2025("AX2025", 2025)
}
