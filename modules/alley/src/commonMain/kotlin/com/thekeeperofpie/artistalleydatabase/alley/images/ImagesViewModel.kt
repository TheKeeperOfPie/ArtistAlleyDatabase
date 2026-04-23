package com.thekeeperofpie.artistalleydatabase.alley.images

import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import dev.zacsweers.metro.Inject

@Inject
class ImagesViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val stampRallyEntryDao: StampRallyEntryDao,
) : ViewModel() {

    suspend fun load(
        route: AlleyDestination.Images,
    ): Pair<AlleyDestination.Images.Type, List<CatalogImage>>? =
        when (val type = route.type) {
            is AlleyDestination.Images.Type.Artist -> {
                val artist = artistEntryDao.getEntry(route.year, route.id)?.artist ?: return null
                val fallbackImageYear = artist.fallbackImageYear
                val showingFallback = type.showingFallback && fallbackImageYear != null
                type.copy(
                    booth = type.booth ?: artist.booth,
                    name = type.name ?: artist.name,
                    profileImage = type.profileImage
                        ?: AlleyImageUtils.getProfileImage(artist.embeds),
                ) to AlleyImageUtils.getArtistImagesWithEmbedFallback(
                    if (showingFallback) {
                        fallbackImageYear
                    } else {
                        artist.year
                    },
                    if (showingFallback) {
                        artist.fallbackImages
                    } else {
                        artist.images
                    },
                    embeds = artist.embeds,
                )
            }
            is AlleyDestination.Images.Type.StampRally -> {
                val stampRally = stampRallyEntryDao.getEntry(route.year, route.id)
                    ?.stampRally ?: return null
                type.copy(
                    hostTable = type.hostTable ?: stampRally.hostTable,
                    fandom = type.fandom ?: stampRally.fandom,
                ) to AlleyImageUtils.getRallyImages(stampRally.year, stampRally.images)
            }
        }
}
