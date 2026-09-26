package com.thekeeperofpie.artistalleydatabase.alley.images

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.alley.AlleyDestination
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.changelog.catalogImages
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlin.uuid.Uuid

@AssistedInject
class ImagesViewModel(
    private val artistEntryDao: ArtistEntryDao,
    private val stampRallyEntryDao: StampRallyEntryDao,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    suspend fun load(
        route: AlleyDestination.Images,
    ): Pair<AlleyDestination.Images.Type, List<CatalogImage>>? =
        when (val type = route.type) {
            is AlleyDestination.Images.Type.Artist -> {
                val entry = artistEntryDao.getEntry(route.year, route.id)
                val artist = entry?.artist ?: return null
                val artistImages = if (route.changelogDate != null) {
                    val changelog =
                        artistEntryDao.getChangelogEntry(route.year, Uuid.parse(route.id), route.changelogDate)
                            ?: return null
                    changelog.catalogImages(route.year) ?: return null
                } else {
                    val fallbackImageYear = artist.fallbackImageYear
                    val showingFallback = type.showingFallback && fallbackImageYear != null
                    AlleyImageUtils.getArtistImagesWithEmbedFallback(
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
                        tempImages = artist.tempImages,
                        embeds = artist.embeds,
                    )
                }
                type.copy(
                    booth = type.booth ?: artist.booth,
                    name = type.name ?: artist.name,
                    profileImage = type.profileImage ?: entry.profileImage,
                ) to artistImages
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

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(savedStateHandle: SavedStateHandle): ImagesViewModel
    }
}
