package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import kotlin.random.Random

class ArtistEntryGridModel(
    private val randomSeed: Int,
    private val showOnlyConfirmedTags: Boolean,
    val artist: ArtistEntry,
    val userEntry: ArtistUserEntry,
    override val images: List<CatalogImage>,
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

    val series by lazy {
        val random = Random(randomSeed)
        val list = artist.seriesConfirmed.shuffled(random).toMutableList()
        if (!showOnlyConfirmedTags) {
            list += artist.seriesInferred.shuffled(random)
        }
        list.distinct()
    }

    val merch by lazy {
        val random = Random(randomSeed)
        val list = artist.merchConfirmed.shuffled(random).toMutableList()
        if (!showOnlyConfirmedTags) {
            list += artist.merchInferred.shuffled(random)
        }
        list.distinct()
    }

    companion object {
        fun buildFromEntry(
            randomSeed: Int,
            showOnlyConfirmedTags: Boolean,
            entry: ArtistWithUserData,
        ) = ArtistEntryGridModel(
            randomSeed = randomSeed,
            showOnlyConfirmedTags = showOnlyConfirmedTags,
            artist = entry.artist,
            userEntry = entry.userEntry,
            images = AlleyDataUtils.getImages(
                year = entry.artist.year,
                folder = AlleyDataUtils.Folder.CATALOGS,
                file = entry.artist.booth,
            ),
            placeholderText = entry.artist.booth ?: entry.artist.name,
        )
    }
}
