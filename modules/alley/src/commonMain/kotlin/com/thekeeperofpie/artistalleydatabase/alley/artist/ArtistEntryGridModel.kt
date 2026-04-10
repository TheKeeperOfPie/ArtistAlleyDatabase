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
    val showOutdatedCatalogs: Boolean,
    override val images: List<CatalogImage>,
    val profileImage: CatalogImage?,
    override val placeholderText: String,
) : SearchScreen.SearchEntryModel {

    override val id = EntryId("artist_entry", artist.id)
    override val imageUri: Uri? = null
    override val imageWidth get() = 0
    override val imageHeight get() = 0
    override val imageWidthToHeightRatio get() = 1f

    override val fallbackImages: List<CatalogImage> = artist.fallbackImageYear
        ?.takeIf { showOutdatedCatalogs }
        ?.let {
            AlleyImageUtils.getArtistImages(year = it, images = artist.fallbackImages)
        }.orEmpty()
    override val fallbackYear: DataYear?
        get() = artist.fallbackImageYear?.takeIf { showOutdatedCatalogs }
    override var favorite by mutableStateOf(userEntry.favorite)
    override var ignored by mutableStateOf(userEntry.ignored)

    override val booth get() = artist.booth

    companion object {

        fun buildFromEntry(
            randomSeed: Int,
            showOnlyConfirmedTags: Boolean,
            entry: ArtistWithUserData,
            showOutdatedCatalogs: Boolean, // TODO: Move this to UI layer?
        ): ArtistEntryGridModel {
            val artist = entry.artist
            val merch = TagUtils.combineForDisplay(
                inferred = artist.merchInferred,
                confirmed = artist.merchConfirmed,
                randomSeed = randomSeed,
                showOnlyConfirmedTags = showOnlyConfirmedTags,
            )

            val series = TagUtils.combineForDisplay(
                inferred = artist.seriesInferred,
                confirmed = artist.seriesConfirmed,
                randomSeed = randomSeed,
                showOnlyConfirmedTags = showOnlyConfirmedTags,
            )

            val images = AlleyImageUtils.getArtistImagesWithEmbedFallback(
                year = artist.year,
                images = artist.images,
                embeds = artist.embeds,
            )

            val profileImage = AlleyImageUtils.getProfileImage(artist.embeds)

            return ArtistEntryGridModel(
                artist = artist,
                userEntry = entry.userEntry,
                series = series.take(TagUtils.TAGS_TO_SHOW),
                hasMoreSeries = series.size > TagUtils.TAGS_TO_SHOW,
                merch = merch.take(TagUtils.TAGS_TO_SHOW),
                hasMoreMerch = merch.size > TagUtils.TAGS_TO_SHOW,
                showOutdatedCatalogs = showOutdatedCatalogs,
                images = images,
                profileImage = profileImage,
                placeholderText = artist.booth ?: artist.name,
            )
        }
    }
}
