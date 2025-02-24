package com.thekeeperofpie.artistalleydatabase.alley.data

import kotlinx.serialization.Serializable

@Serializable
enum class DataYear(val year: Int) {
    YEAR_2023(2023), YEAR_2024(2024), YEAR_2025(2025)
}
