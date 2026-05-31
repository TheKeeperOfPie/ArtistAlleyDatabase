package com.thekeeperofpie.artistalleydatabase.alley.changelog

import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

data class StampRallyChangelogEntry(
    val stampRallyId: Uuid,
    val date: LocalDate,
    val images: List<CatalogImage>,
    val rally: StampRallyEntryAnimeExpo2026,
)

fun StampRallyEntryAnimeExpo2026Changelog.toChangelogEntry(
    dataYear: DataYear,
    allRallies: Map<Uuid, StampRallyEntryAnimeExpo2026>,
): StampRallyChangelogEntry? {
    val rally = allRallies[stampRallyId] ?: return null
    return StampRallyChangelogEntry(
        stampRallyId = stampRallyId,
        date = LocalDate.parse(date),
        images = AlleyImageUtils.getRallyImages(dataYear, images.orEmpty()),
        rally = rally,
    )
}
