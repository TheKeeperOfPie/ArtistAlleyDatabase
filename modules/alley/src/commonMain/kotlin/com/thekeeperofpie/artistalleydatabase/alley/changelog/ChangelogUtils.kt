package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

fun ArtistEntryAnimeExpo2026Changelog.catalogImages(year: DataYear): List<CatalogImage>? {
    val images = images
    return when {
        images == null -> null
        isTempImages -> AlleyImageUtils.getTempImages(images)
        else -> AlleyImageUtils.getArtistImages(year, images)
    }
}

fun List<ArtistEntryAnimeExpo2026Changelog>.sortArtistsForChangelog() =
    sortedBy { it.booth }.sortedBy { it.images.isNullOrEmpty() }

fun List<StampRallyChangelogEntry>.sortRalliesForChangelog() = sortedBy { it.rally.fandom }

internal object ChangelogUtils {
    val ImageHeight = 200.dp
}
