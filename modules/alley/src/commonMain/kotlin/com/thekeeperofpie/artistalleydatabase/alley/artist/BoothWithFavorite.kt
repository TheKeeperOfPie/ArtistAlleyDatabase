package com.thekeeperofpie.artistalleydatabase.alley.artist

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

data class BoothWithFavorite(
    val year: DataYear,
    val id: String,
    val booth: String?,
    val name: String?,
    val images: List<CatalogImage>,
    val favorite: Boolean,
)
