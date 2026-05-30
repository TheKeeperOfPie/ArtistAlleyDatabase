package com.thekeeperofpie.artistalleydatabase.alley.changelog

import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

data class StampRallyChangelogEntry(
    val stampRallyId: Uuid,
    val date: LocalDate,
    val images: List<CatalogImage>,
    val rally: StampRallyEntryAnimeExpo2026,
)
