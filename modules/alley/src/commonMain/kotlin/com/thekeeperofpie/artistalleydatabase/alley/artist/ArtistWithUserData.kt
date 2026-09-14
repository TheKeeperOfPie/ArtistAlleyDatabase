package com.thekeeperofpie.artistalleydatabase.alley.artist

import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry

data class ArtistWithUserData(
    val artist: ArtistEntry,
    val userEntry: ArtistUserEntry,
) {
    // TODO: Move these fields into ArtistEntry?
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

    fun showingFallback(showOutdatedCatalogs: Boolean): Boolean =
        showOutdatedCatalogs && artist.images.isEmpty() && fallbackImages.isNotEmpty()

    fun displayImages(showOutdatedCatalogs: Boolean): List<CatalogImage> =
        if (showingFallback(showOutdatedCatalogs)) fallbackImages else images
}
