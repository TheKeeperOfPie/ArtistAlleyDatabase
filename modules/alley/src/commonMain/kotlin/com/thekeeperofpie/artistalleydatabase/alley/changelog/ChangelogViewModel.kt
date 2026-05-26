package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

@AssistedInject
class ChangelogViewModel(
    artistEntryDao: ArtistEntryDao,
    dispatchers: CustomDispatchers,
    val seriesEntryCache: SeriesEntryCache,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val catalogsOnly = savedStateHandle.getMutableStateFlow("catalogsOnly", false)

    internal val changes = catalogsOnly.mapLatest { artistEntryDao.getChangelog(it) }
        .mapLatest {
            it.groupBy { LocalDate.parse(it.date) }
                .toList()
                .sortedByDescending { it.first }
                .map {
                    val (added, updated) = it.second.partition { it.isBrandNew }
                    ChangelogScreen.DayChange(
                        date = it.first,
                        added = added.sortedBy { it.booth },
                        updated = updated.sortedBy { it.booth },
                    )
                }
        }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): ChangelogViewModel
    }
}
