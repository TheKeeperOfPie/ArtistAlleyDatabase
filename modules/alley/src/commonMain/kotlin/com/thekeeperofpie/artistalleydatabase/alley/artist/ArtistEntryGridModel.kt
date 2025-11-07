package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.SeriesEntry
import com.thekeeperofpie.artistalleydatabase.alley.images.AlleyImageUtils
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchScreen
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.entry.EntryId
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlin.random.Random

class ArtistEntryGridModel(
    val artist: ArtistEntry,
    val userEntry: ArtistUserEntry,
    val series: List<SeriesEntry>,
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
        const val TAGS_TO_SHOW = 5

        internal suspend fun getSeriesAndHasMore(
            randomSeed: Int,
            showOnlyConfirmedTags: Boolean,
            entry: ArtistWithUserData,
            seriesEntryCache: SeriesEntryCache,
        ): Pair<List<SeriesEntry>, Boolean> {
            val seriesIds = entry.artist.seriesConfirmed.toMutableList()
            if (!showOnlyConfirmedTags && seriesIds.size < TAGS_TO_SHOW) {
                seriesIds += entry.artist.seriesInferred
            }
            val idsToQuery = seriesIds.shuffled(Random(randomSeed)).take(TAGS_TO_SHOW)
            return seriesEntryCache.getSeries(idsToQuery) to (seriesIds.size > TAGS_TO_SHOW)
        }

        fun buildFromEntry(
            randomSeed: Int,
            showOnlyConfirmedTags: Boolean,
            entry: ArtistWithUserData,
            series: List<SeriesEntry>,
            hasMoreSeries: Boolean,
            showOutdatedCatalogs: Boolean, // TODO: Move this to UI layer?
            fallbackCatalog: Pair<DataYear, List<CatalogImage>>?,
        ): ArtistEntryGridModel {
            val random = Random(randomSeed)
            var merch = entry.artist.merchConfirmed.shuffled(random)
            if (!showOnlyConfirmedTags && merch.size < TAGS_TO_SHOW) {
                merch = merch + entry.artist.merchInferred.shuffled(random)
            }
            val images = AlleyImageUtils.getArtistImages(
                year = entry.artist.year,
                images = entry.artist.images,
            )

            return ArtistEntryGridModel(
                artist = entry.artist,
                userEntry = entry.userEntry,
                series = series,
                hasMoreSeries = hasMoreSeries,
                merch = merch,
                hasMoreMerch = merch.size > TAGS_TO_SHOW,
                images = images,
                fallbackImages = fallbackCatalog?.second?.takeIf { showOutdatedCatalogs }.orEmpty(),
                fallbackYear = fallbackCatalog?.first?.takeIf { showOutdatedCatalogs },
                placeholderText = entry.artist.booth ?: entry.artist.name,
            )
        }
    }
}
