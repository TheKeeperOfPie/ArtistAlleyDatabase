package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagUtils
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

class ArtistEntryGridModel(
    val artist: ArtistEntry,
    val userEntry: ArtistUserEntry,
    val series: List<String>,
    val hasMoreSeries: Boolean,
    val merch: List<String>,
    val hasMoreMerch: Boolean,
    override val images: List<CatalogImage>,
    override val fallbackImages: List<CatalogImage>,
    override val fallbackYear: DataYear?,
    override val placeholderText: String,
) : SearchScreen.SearchEntryModel {

    override val id = EntryId("artist_entry", artist.id)
    override val imageUri: Uri? = null
    override val imageWidth get() = 0
    override val imageHeight get() = 0
    override val imageWidthToHeightRatio get() = 1f

    override var favorite by mutableStateOf(userEntry.favorite)
    override var ignored by mutableStateOf(userEntry.ignored)

    override val booth get() = artist.booth

    companion object {

        fun buildFromEntry(
            randomSeed: Int,
            showOnlyConfirmedTags: Boolean,
            entry: ArtistWithUserData,
            showOutdatedCatalogs: Boolean, // TODO: Move this to UI layer?
            fallbackCatalog: Pair<DataYear, List<CatalogImage>>?,
        ): ArtistEntryGridModel {
            val merch = TagUtils.combineForDisplay(
                inferred = entry.artist.merchInferred,
                confirmed = entry.artist.merchConfirmed,
                randomSeed = randomSeed,
                showOnlyConfirmedTags = showOnlyConfirmedTags,
            )

            val series = TagUtils.combineForDisplay(
                inferred = entry.artist.seriesInferred,
                confirmed = entry.artist.seriesConfirmed,
                randomSeed = randomSeed,
                showOnlyConfirmedTags = showOnlyConfirmedTags,
            )

            val images = AlleyImageUtils.getArtistImages(
                year = entry.artist.year,
                images = entry.artist.images,
            )

            return ArtistEntryGridModel(
                artist = entry.artist,
                userEntry = entry.userEntry,
                series = series.take(TagUtils.TAGS_TO_SHOW),
                hasMoreSeries = series.size > TagUtils.TAGS_TO_SHOW,
                merch = merch.take(TagUtils.TAGS_TO_SHOW),
                hasMoreMerch = merch.size > TagUtils.TAGS_TO_SHOW,
                images = images,
                fallbackImages = fallbackCatalog?.second?.takeIf { showOutdatedCatalogs }.orEmpty(),
                fallbackYear = fallbackCatalog?.first?.takeIf { showOutdatedCatalogs },
                placeholderText = entry.artist.booth ?: entry.artist.name,
            )
        }
    }
}
