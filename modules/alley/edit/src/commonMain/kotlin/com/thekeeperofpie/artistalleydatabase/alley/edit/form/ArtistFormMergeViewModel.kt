package com.thekeeperofpie.artistalleydatabase.alley.edit.form

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.stateIn
import kotlin.uuid.Uuid

@AssistedInject
class ArtistFormMergeViewModel(
    database: AlleyEditDatabase,
    dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    @Assisted dataYear: DataYear,
    @Assisted artistId: Uuid,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val entry = flowFromSuspend { database.loadArtistWithFormEntry(dataYear, artistId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val seriesById = flowFromSuspend { database.loadSeries() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    val merchById = flowFromSuspend { database.loadMerch() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    fun seriesImage(info: SeriesInfo) = imageLoader.getSeriesImage(info.toImageInfo())

    fun onClickSave() {
        TODO()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            dataYear: DataYear,
            artistId: Uuid,
            savedStateHandle: SavedStateHandle,
        ): ArtistFormMergeViewModel
    }
}
