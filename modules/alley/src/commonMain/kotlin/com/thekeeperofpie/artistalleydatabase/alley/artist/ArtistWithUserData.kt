package com.thekeeperofpie.artistalleydatabase.alley.artist

import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry

data class ArtistWithUserData(
    val artist: ArtistEntry,
    val userEntry: ArtistUserEntry,
) {
    val profileImage = AlleyImageUtils.getProfileImage(artist.year, artist.profileImage)

    val fallbackImages: List<CatalogImage> = artist.fallbackImageYear
        ?.let { AlleyImageUtils.getArtistImages(year = it, images = artist.fallbackImages) }
        .orEmpty()

    val images = AlleyImageUtils.getArtistImagesWithEmbedFallback(
        year = artist.year,
        images = artist.images,
        tempImages = artist.tempImages,
        embeds = artist.embeds,
    )
}
