package com.thekeeperofpie.artistalleydatabase.alley.artist

import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear

data class BoothWithFavorite(
    val year: DataYear,
    val id: String,
    val booth: String?,
    val favorite: Boolean?,
)
