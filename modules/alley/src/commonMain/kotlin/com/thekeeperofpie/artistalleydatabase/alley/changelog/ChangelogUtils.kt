package com.thekeeperofpie.artistalleydatabase.alley.changelog

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
