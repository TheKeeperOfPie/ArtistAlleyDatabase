package com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.changelog.ChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.changelog.toChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
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

@AssistedInject
class FavoriteSeriesChangelogViewModel(
    dispatchers: CustomDispatchers,
    val seriesEntryCache: SeriesEntryCache,
    private val seriesImageLoader: SeriesImageLoader,
    settings: ArtistAlleySettings,
    useCaseFactory: FavoritesChangelogUseCase.Factory,
    userEntryDao: UserEntryDao,
    @Assisted private val dataYear: DataYear,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val showOnlyConfirmedTags = savedStateHandle.getMutableStateFlow(
        key = "showOnlyConfirmedTags",
        initialValue = settings.showOnlyConfirmedTags.value,
    )

    private val randomSeed =
        savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }

    val useCase = useCaseFactory.create(
        dataYear = dataYear,
        randomSeed = randomSeed,
        input = combine(showOnlyConfirmedTags, userEntryDao.getSeriesFavorites(), ::Input),
        filterArtist = { input ->
            (artist.seriesConfirmed?.any { it in input.favoriteSeriesIds } == true) ||
                    (!input.showOnlyConfirmedTags && (artist.seriesInferred?.any { it in input.favoriteSeriesIds } == true))
        },
        filterRally = { input ->
            rally.series.any { it in input.favoriteSeriesIds }
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
            showOnlyConfirmedTags = changes.input.showOnlyConfirmedTags,
            seriesIdsToHighlight = changes.input.favoriteSeriesIds,
            merchIdsToHighlight = emptySet()
        )

    fun seriesImage(seriesId: String) = seriesImageLoader.getSeriesImage(seriesId)

    data class Input(
        val showOnlyConfirmedTags: Boolean,
        val favoriteSeriesIds: Set<String>,
    )
}
