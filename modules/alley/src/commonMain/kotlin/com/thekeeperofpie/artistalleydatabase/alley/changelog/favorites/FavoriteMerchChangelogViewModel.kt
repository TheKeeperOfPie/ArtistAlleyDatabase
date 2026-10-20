package com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.changelog.ChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.changelog.toChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
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
class FavoriteMerchChangelogViewModel(
    val seriesEntryCache: SeriesEntryCache,
    settings: ArtistAlleySettings,
    useCaseFactory: FavoritesChangelogUseCase.Factory,
    userEntryDao: UserEntryDao,
    @Assisted private val dataYear: DataYear,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var showOnlyConfirmedTags by savedStateHandle.saveable { mutableStateOf(settings.showOnlyConfirmedTags) }

    private val randomSeed =
        savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }

    private val input by transform(viewModelScope) {
        val merchFavorites by produceState(emptySet()) {
            userEntryDao.getMerchFavorites().collectLatest {  value = it }
        }
        Input(showOnlyConfirmedTags, merchFavorites)
    }

    val useCase = useCaseFactory.create(
        scope = viewModelScope,
        dataYear = dataYear,
        randomSeed = randomSeed,
        input = ::input,
        filterArtist = { input ->
            (artist.merchConfirmed?.any { it in input.favoriteMerchIds } == true) ||
                    (!input.showOnlyConfirmedTags && (artist.merchInferred?.any { it in input.favoriteMerchIds } == true))
        },
        filterRally = { input ->
            rally.merch.any { it in input.favoriteMerchIds }
        },
    )

    internal val changes by transform(viewModelScope) {
        val changes = useCase.changes
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

    private fun ChangelogEntry.Artist.toChangelogEntry(changes: FavoritesChangelogUseCase.Changes<Input>) =
        artist.toChangelogEntry(
            dataYear = dataYear,
            randomSeed = randomSeed,
            showOnlyConfirmedTags = changes.input.showOnlyConfirmedTags,
            seriesIdsToHighlight = emptySet(),
            merchIdsToHighlight = changes.input.favoriteMerchIds,
        )

    data class Input(
        val showOnlyConfirmedTags: Boolean,
        val favoriteMerchIds: Set<String>,
    )

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            dataYear: DataYear,
            savedStateHandle: SavedStateHandle,
        ): FavoriteMerchChangelogViewModel
    }
}
