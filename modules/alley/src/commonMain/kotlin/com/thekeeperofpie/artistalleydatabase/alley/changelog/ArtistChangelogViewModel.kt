package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
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
class ArtistChangelogViewModel(
    artistEntryDao: ArtistEntryDao,
    dispatchers: CustomDispatchers,
    val seriesEntryCache: SeriesEntryCache,
    val settings: ArtistAlleySettings,
    userEntryDao: UserEntryDao,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val catalogsOnly = savedStateHandle.getMutableStateFlow("catalogsOnly", false)

    private val randomSeed =
        savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }

    internal val changes = combine(userEntryDao.getTagFavorites(), catalogsOnly, ::Pair)
        .mapLatest { (tagFavorites, catalogsOnly) ->
            artistEntryDao.getChangelog(catalogsOnly)
                .asSequence()
                .map {
                    it.toChangelogEntry(
                        // TODO: Split by year
                        dataYear = DataYear.ANIME_EXPO_2026,
                        randomSeed = randomSeed,
                        showOnlyConfirmedTags = catalogsOnly,
                        seriesIdsToHighlight = tagFavorites.seriesIds,
                        merchIdsToHighlight = tagFavorites.merchIds,
                    )
                }
                .groupBy { it.date }
                .toList()
                .sortedByDescending { it.first }
                .map {
                    val (added, updated) = it.second.partition { it.isBrandNew }
                    ArtistChangelogScreen.DayChange(
                        date = it.first,
                        added = added.sortArtistsForChangelog(),
                        updated = updated.sortArtistsForChangelog(),
                    )
                }
                .toList()
        }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

}
