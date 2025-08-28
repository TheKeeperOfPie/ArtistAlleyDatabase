package com.thekeeperofpie.artistalleydatabase.alley.details

import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

data class DetailsScreenCatalog(
    val images: List<CatalogImage>,
    val showOutdatedCatalogs: Boolean?,
    val fallbackYear: DataYear?,
)
