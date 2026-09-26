package com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.changelog.sortRalliesForChangelog
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.math.absoluteValue
import kotlin.random.Random

@AssistedInject
class FavoriteRalliesChangelogViewModel(
    dispatchers: CustomDispatchers,
    val seriesEntryCache: SeriesEntryCache,
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
            userEntryDao.getRallyFavorites(),
            userEntryDao.getTagFavorites(),
        ) { favoriteRallyIds, tagFavorites ->
            Input(
                favoriteRallyIds = favoriteRallyIds,
                favoriteSeriesIds = tagFavorites.seriesIds,
                favoriteMerchIds = tagFavorites.merchIds
            )
        },
        filterArtist = { false },
        filterRally = {
            // TODO: 2023 stamp rallies should crash because of not being UUIDs
            stampRallyId.toString() in it.favoriteRallyIds
        },
    )

    internal val changes = useCase.changes
        .mapLatest { changes ->
            changes.changes.map {
                FavoritesChangelogScreen.DayChange(
                    date = it.date,
                    addedArtists = emptyList(),
                    updatedArtists = emptyList(),
                    addedRallies = it.addedRallies.sortRalliesForChangelog(),
                    updatedRallies = it.updatedRallies.sortRalliesForChangelog(),
                )
            }
        }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    data class Input(
        val favoriteRallyIds: Set<String>,
        val favoriteSeriesIds: Set<String>,
        val favoriteMerchIds: Set<String>,
    )

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            dataYear: DataYear,
            savedStateHandle: SavedStateHandle,
        ): FavoriteRalliesChangelogViewModel
    }
}
