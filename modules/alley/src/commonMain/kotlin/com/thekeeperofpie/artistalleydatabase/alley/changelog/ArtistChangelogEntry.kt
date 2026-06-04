package com.thekeeperofpie.artistalleydatabase.alley.changelog

import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.datetime.LocalDate
import kotlin.random.Random
import kotlin.uuid.Uuid

data class ArtistChangelogEntry(
    val artistId: Uuid,
    val date: LocalDate,
    val booth: String?,
    val name: String,
    val seriesHighlighted: List<String>,
    val seriesRemaining: List<String>,
    val merchHighlighted: List<String>,
    val merchRemaining: List<String>,
    val isBrandNew: Boolean,
    val images: List<CatalogImage>,
)

fun ArtistEntryAnimeExpo2026Changelog.toChangelogEntry(
    dataYear: DataYear,
    randomSeed: Int,
    showOnlyConfirmedTags: Boolean,
    seriesIdsToHighlight: Set<String> = emptySet(),
    merchIdsToHighlight: Set<String> = emptySet(),
): ArtistChangelogEntry {
    val random = Random(randomSeed)
    val allSeries = if (showOnlyConfirmedTags) {
        seriesConfirmed.orEmpty()
    } else {
        seriesConfirmed.orEmpty() + seriesInferred.orEmpty()
    }.shuffled(random)
    val (seriesHighlighted, seriesRemaining) = allSeries.partition { it in seriesIdsToHighlight }

    val allMerch = if (showOnlyConfirmedTags) {
        merchConfirmed.orEmpty()
    } else {
        merchConfirmed.orEmpty() + merchInferred.orEmpty()
    }.shuffled(random)
    val (merchHighlighted, merchRemaining) = allMerch.partition { it in merchIdsToHighlight }

    return ArtistChangelogEntry(
        artistId = artistId,
        date = LocalDate.parse(date),
        booth = booth,
        name = name,
        seriesHighlighted = seriesHighlighted,
        seriesRemaining = seriesRemaining,
        merchHighlighted = merchHighlighted,
        merchRemaining = merchRemaining,
        isBrandNew = isBrandNew,
        images = catalogImages(dataYear).orEmpty(),
    )
}
