package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.uuid.Uuid

@AssistedInject
class ArtistHistoryViewModel(
    database: AlleyEditDatabase,
    dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    @Assisted private val dataYear: DataYear,
    @Assisted private val artistId: Uuid,
): ViewModel() {
    val history = flowFromSuspend { database.loadArtistHistory(dataYear, artistId) }
        .mapLatest(ArtistHistoryEntryWithDiff::calculateDiffs)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val seriesById = flowFromSuspend { database.loadSeries() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    val merchById = flowFromSuspend { database.loadMerch() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            artistId: Uuid,
        ): ArtistHistoryViewModel
    }
}
