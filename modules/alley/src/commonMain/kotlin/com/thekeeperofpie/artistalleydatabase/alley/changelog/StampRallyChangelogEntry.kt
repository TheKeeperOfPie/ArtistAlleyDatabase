package com.thekeeperofpie.artistalleydatabase.alley.changelog

import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.data.StampRallyEntryAnimeExpo2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.datetime.LocalDate
import kotlin.random.Random
import kotlin.uuid.Uuid

data class StampRallyChangelogEntry(
    val stampRallyId: Uuid,
    val date: LocalDate,
    val images: List<CatalogImage>,
    val rally: StampRallyEntryAnimeExpo2026,
    val isBrandNew: Boolean,
    val seriesHighlighted: List<String>,
    val seriesRemaining: List<String>,
    val merchHighlighted: List<String>,
    val merchRemaining: List<String>,
)

fun StampRallyEntryAnimeExpo2026Changelog.toChangelogEntry(
    dataYear: DataYear,
    allRallies: Map<Uuid, StampRallyEntryAnimeExpo2026>,
    randomSeed: Int,
    seriesIdsToHighlight: Set<String> = emptySet(),
    merchIdsToHighlight: Set<String> = emptySet(),
): StampRallyChangelogEntry? {
    val rally = allRallies[stampRallyId] ?: return null
    val random = Random(randomSeed)
    val (seriesHighlighted, seriesRemaining) = rally.series.shuffled(random)
        .partition { it in seriesIdsToHighlight }
    val (merchHighlighted, merchRemaining) = (rally.prizeMerch.orEmpty() + rally.merch).shuffled(random)
        .partition { it in merchIdsToHighlight }
    return StampRallyChangelogEntry(
        stampRallyId = stampRallyId,
        date = LocalDate.parse(date),
        images = AlleyImageUtils.getRallyImages(dataYear, images.orEmpty()),
        rally = rally,
        isBrandNew = isBrandNew,
        seriesHighlighted = seriesHighlighted,
        seriesRemaining = seriesRemaining,
        merchHighlighted = merchHighlighted,
        merchRemaining = merchRemaining,
    )
}
