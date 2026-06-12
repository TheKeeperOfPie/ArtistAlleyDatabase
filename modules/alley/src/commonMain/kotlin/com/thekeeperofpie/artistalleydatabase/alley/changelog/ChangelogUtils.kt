package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlin.jvm.JvmName

fun ArtistEntryAnimeExpo2026Changelog.catalogImages(year: DataYear): List<CatalogImage>? {
    val images = images
    return when {
        images == null -> null
        isTempImages -> AlleyImageUtils.getTempImages(images)
        else -> AlleyImageUtils.getArtistImages(year, images)
    }
}

fun List<ArtistChangelogEntry>.sortArtistsForChangelog() =
    sortedWith(compareBy({ it.images.isEmpty() }, { it.booth }))

@JvmName("sortArtistsForChangelogChangelogEntry")
fun List<ChangelogEntry.Artist>.sortArtistsForChangelog() =
    sortedWith(compareBy({ it.artist.images.isNullOrEmpty() }, { it.artist.booth }))

fun List<StampRallyChangelogEntry>.sortRalliesForChangelog() =
    sortedWith(compareBy({ it.images.isEmpty() }, { it.rally.fandom }))

internal object ChangelogUtils {
    val ImageHeight = 200.dp
}
