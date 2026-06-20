package com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.changelog.ChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.changelog.toChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.uuid.Uuid

@AssistedInject
class FavoriteArtistsChangelogViewModel(
    dispatchers: CustomDispatchers,
    val seriesEntryCache: SeriesEntryCache,
    private val seriesImageLoader: SeriesImageLoader,
    useCaseFactory: FavoritesChangelogUseCase.Factory,
    userEntryDao: UserEntryDao,
    @Assisted private val dataYear: DataYear,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val randomSeed =
        savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }

    val useCase = useCaseFactory.create(
        dataYear = dataYear,
        randomSeed = randomSeed,
        input = combine(
            userEntryDao.getArtistFavorites(dataYear)
                .mapLatest {
                    // TODO: Are all artists guaranteed to be UUIDs?
                    it.map { Uuid.parse(it) }.toSet()
                },
            userEntryDao.getTagFavorites(),
        ) { favoriteArtists, tagFavorites ->
            Input(
                favoriteArtistIds = favoriteArtists,
                favoriteSeriesIds = tagFavorites.seriesIds,
                favoriteMerchIds = tagFavorites.merchIds
            )
        },
        filterArtist = { input ->
            this.artist.artistId in input.favoriteArtistIds
        },
        filterRally = {
            // TODO: Include rallies by favorite artists
            false
        },
    )

    internal val changes = useCase.changes
        .mapLatest { changes ->
            changes.changes.map {
                FavoritesChangelogScreen.DayChange(
                    date = it.date,
                    addedArtists = it.addedArtists.map { it.toChangelogEntry(changes) },
                    updatedArtists = it.updatedArtists.map { it.toChangelogEntry(changes) },
                    addedRallies = it.addedRallies,
                    updatedRallies = it.updatedRallies,
                )
            }
        }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private fun ChangelogEntry.Artist.toChangelogEntry(changes: FavoritesChangelogUseCase.Changes<Input>) =
        artist.toChangelogEntry(
            dataYear = dataYear,
            randomSeed = randomSeed,
            showOnlyConfirmedTags = false,
            seriesIdsToHighlight = changes.input.favoriteSeriesIds,
            merchIdsToHighlight = changes.input.favoriteMerchIds,
        )

    fun seriesImage(seriesId: String) = seriesImageLoader.getSeriesImage(seriesId)

    data class Input(
        val favoriteArtistIds: Set<Uuid>,
        val favoriteSeriesIds: Set<String>,
        val favoriteMerchIds: Set<String>,
    )
}
