package com.thekeeperofpie.artistalleydatabase.alley.rallies

import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

data class StampRallyChangelogWithSeries(
    val stampRallyId: Uuid,
    val date: LocalDate,
    val images: List<CatalogImage>,
)
