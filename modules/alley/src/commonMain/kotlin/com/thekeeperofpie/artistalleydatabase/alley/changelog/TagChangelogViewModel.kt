package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@AssistedInject
class TagChangelogViewModel(
    artistEntryDao: ArtistEntryDao,
    stampRallyEntryDao: StampRallyEntryDao,
    dispatchers: CustomDispatchers,
    val seriesEntryCache: SeriesEntryCache,
    private val seriesImageLoader: SeriesImageLoader,
    settings: ArtistAlleySettings,
    @Assisted dataYear: DataYear,
    @Assisted private val series: String? = null,
    @Assisted private val merch: String? = null,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val showOnlyConfirmedTags = savedStateHandle.getMutableStateFlow(
        key = "showOnlyConfirmedTags",
        initialValue = settings.showOnlyConfirmedTags.value,
    )

    internal val changes = flowFromSuspend {
        val artists = artistEntryDao.getChangelog(false).map(TagChangelogEntry::Artist)
        val allRallies = stampRallyEntryDao.getAllEntriesForChangelog(dataYear)
        val stampRallies = stampRallyEntryDao.getChangelog(dataYear)
            .mapNotNull { it.toChangelogEntry(dataYear, allRallies) }
            .map(TagChangelogEntry::StampRally)
        artists + stampRallies
    }
        .mapLatest {
            it.groupBy { it.date }
                .toList()
                .sortedByDescending { it.first }
        }
        .combine(showOnlyConfirmedTags, ::Pair)
        .mapLatest { (changelog, showOnlyConfirmedTags) ->
            changelog.mapNotNull {
                val artists =
                    it.second.filterIsInstance<TagChangelogEntry.Artist>().map { it.artist }
                val rallies = it.second.filterIsInstance<TagChangelogEntry.StampRally>()
                    .map { it.stampRally }
                val (addedArtists, updatedArtists) = artists.filterArtists(showOnlyConfirmedTags)
                    .partition { it.isBrandNew }
                val (addedRallies, updatedRallies) = rallies.filterRallies()
                    .partition { it.images.isEmpty() }
                if (addedArtists.isEmpty() &&
                    updatedArtists.isEmpty() &&
                    addedRallies.isEmpty() &&
                    updatedRallies.isEmpty()
                ) {
                    return@mapNotNull null
                }
                TagChangelogScreen.DayChange(
                    date = it.first,
                    addedArtists = addedArtists.sortArtistsForChangelog(),
                    updatedArtists = updatedArtists.sortArtistsForChangelog(),
                    addedRallies = addedRallies.sortRalliesForChangelog(),
                    updatedRallies = updatedRallies.sortRalliesForChangelog(),
                )
            }
        }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun List<ArtistEntryAnimeExpo2026Changelog>.filterArtists(showOnlyConfirmedTags: Boolean) =
        when {
            series != null -> filter {
                it.seriesConfirmed?.contains(series) == true ||
                        (!showOnlyConfirmedTags && it.seriesInferred?.contains(series) == true)
            }
            merch != null -> filter {
                it.merchConfirmed?.contains(merch) == true ||
                        (!showOnlyConfirmedTags && it.merchInferred?.contains(merch) == true)
            }
            else -> this
        }

    private fun List<StampRallyChangelogEntry>.filterRallies() =
        when {
            series != null -> filter { it.rally.series.contains(series) }
            merch != null -> filter { it.rally.merch.contains(merch) }
            else -> this
        }

    fun seriesImage(seriesId: String) = seriesImageLoader.getSeriesImage(seriesId)

}
