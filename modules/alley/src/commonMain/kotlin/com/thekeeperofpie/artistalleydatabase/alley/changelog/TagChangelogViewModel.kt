package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026Changelog
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@AssistedInject
class TagChangelogViewModel(
    artistEntryDao: ArtistEntryDao,
    seriesEntryDao: SeriesEntryDao,
    merchEntryDao: MerchEntryDao,
    stampRallyEntryDao: StampRallyEntryDao,
    dispatchers: CustomDispatchers,
    val seriesEntryCache: SeriesEntryCache,
    private val seriesImageLoader: SeriesImageLoader,
    settings: ArtistAlleySettings,
    userEntryDao: UserEntryDao,
    @Assisted dataYear: DataYear,
    @Assisted private val seriesId: String? = null,
    @Assisted private val merchId: String? = null,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val showOnlyConfirmedTags = savedStateHandle.getMutableStateFlow(
        key = "showOnlyConfirmedTags",
        initialValue = settings.showOnlyConfirmedTags.value,
    )

    val series = flow {
        if (seriesId != null) {
            emitAll(seriesEntryDao.getSeriesByIdWithUserData(seriesId))
        }
    }.stateInForCompose(null)

    val merch = flow {
        if (merchId != null) {
            emitAll(merchEntryDao.getMerchById(merchId))
        }
    }.stateInForCompose(null)

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

    private val seriesMutationUpdates = MutableSharedFlow<SeriesUserEntry>(5, 5)
    private val merchMutationUpdates = MutableSharedFlow<MerchUserEntry>(5, 5)

    init {
        viewModelScope.launch(dispatchers.io) {
            seriesMutationUpdates.collectLatest {
                userEntryDao.insertSeriesUserEntry(it)
            }
        }
        viewModelScope.launch(dispatchers.io) {
            merchMutationUpdates.collectLatest {
                userEntryDao.insertMerchUserEntry(it)
            }
        }
    }

    private fun List<ArtistEntryAnimeExpo2026Changelog>.filterArtists(showOnlyConfirmedTags: Boolean) =
        when {
            seriesId != null -> filter {
                it.seriesConfirmed?.contains(seriesId) == true ||
                        (!showOnlyConfirmedTags && it.seriesInferred?.contains(seriesId) == true)
            }
            merchId != null -> filter {
                it.merchConfirmed?.contains(merchId) == true ||
                        (!showOnlyConfirmedTags && it.merchInferred?.contains(merchId) == true)
            }
            else -> this
        }

    private fun List<StampRallyChangelogEntry>.filterRallies() =
        when {
            seriesId != null -> filter { it.rally.series.contains(seriesId) }
            merchId != null -> filter { it.rally.merch.contains(merchId) }
            else -> this
        }

    fun seriesImage(seriesId: String) = seriesImageLoader.getSeriesImage(seriesId)

    fun onSeriesFavoriteToggle(data: SeriesWithUserData, favorite: Boolean) {
        seriesMutationUpdates.tryEmit(data.userEntry.copy(favorite = favorite))
    }

    fun onMerchFavoriteToggle(data: MerchWithUserData, favorite: Boolean) {
        merchMutationUpdates.tryEmit(data.userEntry.copy(favorite = favorite))
    }

}
