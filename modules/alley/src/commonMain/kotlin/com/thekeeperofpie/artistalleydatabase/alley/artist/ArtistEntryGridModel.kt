package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.data.AlleyDataUtils
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import kotlin.random.Random

class ArtistEntryGridModel(
    private val randomSeed: Int,
    // TODO: Shove tag filter into UI layer
    private val showOnlyConfirmedTags: Boolean,
    val artist: ArtistEntry,
    val userEntry: ArtistUserEntry,
    val series: List<SeriesEntry>,
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

    val merch by lazy {
        val random = Random(randomSeed)
        val list = artist.merchConfirmed.shuffled(random).toMutableList()
        if (!showOnlyConfirmedTags) {
            list += artist.merchInferred.shuffled(random)
        }
        list
    }

    companion object {
        suspend fun getSeries(
            showOnlyConfirmedTags: Boolean,
            entry: ArtistWithUserData,
            seriesEntryDao: SeriesEntryDao,
        ): List<SeriesEntry> {
            val seriesIds = entry.artist.seriesConfirmed.toMutableList()
            if (!showOnlyConfirmedTags) {
                seriesIds += entry.artist.seriesInferred
            }
            return seriesEntryDao.getSeriesByIds(seriesIds)
        }

        fun buildFromEntry(
            randomSeed: Int,
            showOnlyConfirmedTags: Boolean,
            entry: ArtistWithUserData,
            series: List<SeriesEntry>,
        ): ArtistEntryGridModel {
            return ArtistEntryGridModel(
                randomSeed = randomSeed,
                showOnlyConfirmedTags = showOnlyConfirmedTags,
                artist = entry.artist,
                userEntry = entry.userEntry,
                series = series,
                images = AlleyDataUtils.getArtistImages(
                    year = entry.artist.year,
                    booth = entry.artist.booth,
                    name = entry.artist.name,
                ),
                placeholderText = entry.artist.booth ?: entry.artist.name,
            )
        }
    }
}
