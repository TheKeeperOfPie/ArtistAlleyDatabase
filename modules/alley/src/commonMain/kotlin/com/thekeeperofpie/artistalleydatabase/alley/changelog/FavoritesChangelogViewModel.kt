package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.random.Random

@AssistedInject
class FavoritesChangelogViewModel(
    artistEntryDao: ArtistEntryDao,
    stampRallyEntryDao: StampRallyEntryDao,
    dispatchers: CustomDispatchers,
    val seriesEntryCache: SeriesEntryCache,
    private val seriesImageLoader: SeriesImageLoader,
    settings: ArtistAlleySettings,
    userEntryDao: UserEntryDao,
    @Assisted dataYear: DataYear,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val showOnlyConfirmedTags = savedStateHandle.getMutableStateFlow(
        key = "showOnlyConfirmedTags",
        initialValue = settings.showOnlyConfirmedTags.value,
    )

    private val randomSeed =
        savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }

    private val favorites = userEntryDao.getTagFavorites()
        .mapLatest {
            TagFavorites(seriesIds = it.seriesIds.toSet(), merchIds = it.merchIds.toSet())
        }

    private val changelog = flowFromSuspend {
        val artists = artistEntryDao.getChangelog(false)
            .map(ChangelogEntry::Artist)
        val allRallies = stampRallyEntryDao.getAllEntriesForChangelog(dataYear)
        val stampRallies = stampRallyEntryDao.getChangelog(dataYear)
            .mapNotNull { it.toChangelogEntry(dataYear, allRallies) }
            .map(ChangelogEntry::StampRally)
        artists + stampRallies
    }
        .mapLatest {
            it.groupBy { it.date }
                .toList()
                .sortedByDescending { it.first }
        }

    internal val changes = combine(changelog, showOnlyConfirmedTags, favorites, ::Triple)
        .mapLatest { (changelog, showOnlyConfirmedTags, favorites) ->
            changelog.mapNotNull {
                val artists = it.second.filterIsInstance<ChangelogEntry.Artist>()
                val rallies = it.second.filterIsInstance<ChangelogEntry.StampRally>()
                    .map { it.stampRally }
                val (addedArtists, updatedArtists) =
                    artists.filterArtists(showOnlyConfirmedTags, favorites)
                        .partition { it.artist.isBrandNew }
                val (addedRallies, updatedRallies) = rallies.filterRallies(favorites)
                    .partition { it.images.isEmpty() }
                if (addedArtists.isEmpty() &&
                    updatedArtists.isEmpty() &&
                    addedRallies.isEmpty() &&
                    updatedRallies.isEmpty()
                ) {
                    return@mapNotNull null
                }
                FavoritesChangelogScreen.DayChange(
                    date = it.first,
                    addedArtists = addedArtists.map {
                        it.artist.toChangelogEntry(
                            dataYear = dataYear,
                            randomSeed = randomSeed,
                            showOnlyConfirmedTags = showOnlyConfirmedTags,
                            seriesIdsToHighlight = favorites.seriesIds,
                            merchIdsToHighlight = favorites.merchIds,
                        )
                    }.sortArtistsForChangelog(),
                    updatedArtists = updatedArtists.map {
                        it.artist.toChangelogEntry(
                            dataYear = dataYear,
                            randomSeed = randomSeed,
                            showOnlyConfirmedTags = showOnlyConfirmedTags,
                            seriesIdsToHighlight = favorites.seriesIds,
                            merchIdsToHighlight = favorites.merchIds,
                        )
                    }.sortArtistsForChangelog(),
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

    private fun List<ChangelogEntry.Artist>.filterArtists(
        showOnlyConfirmedTags: Boolean,
        favorites: TagFavorites,
    ) = filter {
        (it.artist.seriesConfirmed?.any { it in favorites.seriesIds } == true) ||
                (!showOnlyConfirmedTags && (it.artist.seriesInferred?.any { it in favorites.seriesIds } == true)) ||
                (it.artist.merchConfirmed?.any { it in favorites.merchIds } == true) ||
                (!showOnlyConfirmedTags && (it.artist.merchInferred?.any { it in favorites.merchIds } == true))
    }

    private fun List<StampRallyChangelogEntry>.filterRallies(favorites: TagFavorites) =
        filter {
            it.rally.series.any { it in favorites.seriesIds } ||
                    it.rally.merch.any { it in favorites.merchIds }
        }

    fun seriesImage(seriesId: String) = seriesImageLoader.getSeriesImage(seriesId)

    private data class TagFavorites(
        val seriesIds: Set<String>,
        val merchIds: Set<String>,
    )

}
