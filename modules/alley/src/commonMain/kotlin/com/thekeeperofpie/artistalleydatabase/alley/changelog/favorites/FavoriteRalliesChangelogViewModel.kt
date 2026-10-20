package com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.changelog.sortRalliesForChangelog
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.transform
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.absoluteValue
import kotlin.random.Random

@AssistedInject
class FavoriteRalliesChangelogViewModel(
    val seriesEntryCache: SeriesEntryCache,
    useCaseFactory: FavoritesChangelogUseCase.Factory,
    userEntryDao: UserEntryDao,
    @Assisted private val dataYear: DataYear,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val randomSeed =
        savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }

    private val input by transform(viewModelScope) {
        val rallyFavorites by produceState(emptySet()) {
            userEntryDao.getRallyFavorites().collectLatest { value = it }
        }
        val tagFavorites by produceState(UserEntryDao.TagFavorites(emptySet(), emptySet())) {
            userEntryDao.getTagFavorites().collectLatest { value = it }
        }
        Input(
            favoriteRallyIds = rallyFavorites,
            favoriteSeriesIds = tagFavorites.seriesIds,
            favoriteMerchIds = tagFavorites.merchIds
        )
    }

    val useCase = useCaseFactory.create(
        scope = viewModelScope,
        dataYear = dataYear,
        randomSeed = randomSeed,
        input = ::input,
        filterArtist = { false },
        filterRally = {
            // TODO: 2023 stamp rallies should crash because of not being UUIDs
            stampRallyId.toString() in it.favoriteRallyIds
        },
    )


    internal val changes by transform(viewModelScope) {
        val changes = useCase.changes
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
