package com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.changelog.ChangelogEntry
import com.thekeeperofpie.artistalleydatabase.alley.changelog.favorites.FavoritesChangelogUseCase.TagFavorites
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
import kotlinx.coroutines.flow.mapLatest
import kotlin.math.absoluteValue
import kotlin.random.Random

@AssistedInject
class FavoritesChangelogViewModel(
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
        val tagFavorites by produceState(TagFavorites(emptySet(), emptySet())) {
            userEntryDao.getTagFavorites()
                .mapLatest {
                    TagFavorites(seriesIds = it.seriesIds.toSet(), merchIds = it.merchIds.toSet())
                }
                .collectLatest { value = it }
        }
        Input(
            showOnlyConfirmedTags = showOnlyConfirmedTags,
            favoriteSeriesIds = tagFavorites.seriesIds,
            favoriteMerchIds = tagFavorites.merchIds,
        )
    }

    val useCase = useCaseFactory.create(
        scope = viewModelScope,
        dataYear = dataYear,
        randomSeed = randomSeed,
        input = ::input,
        filterArtist = { input ->
            (artist.seriesConfirmed?.any { it in input.favoriteSeriesIds } == true) ||
                    (!input.showOnlyConfirmedTags && (artist.seriesInferred?.any { it in input.favoriteSeriesIds } == true)) ||
                    (artist.merchConfirmed?.any { it in input.favoriteMerchIds } == true) ||
                    (!input.showOnlyConfirmedTags && (artist.merchInferred?.any { it in input.favoriteMerchIds } == true))
        },
        filterRally = { input ->
            rally.series.any { it in input.favoriteSeriesIds } ||
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
            seriesIdsToHighlight = changes.input.favoriteSeriesIds,
            merchIdsToHighlight = changes.input.favoriteMerchIds,
        )

    data class Input(
        val showOnlyConfirmedTags: Boolean,
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
        ): FavoritesChangelogViewModel
    }
}
