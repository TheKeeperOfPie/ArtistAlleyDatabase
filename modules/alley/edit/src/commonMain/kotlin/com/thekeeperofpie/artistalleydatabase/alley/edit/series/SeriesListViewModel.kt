package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

@Inject
class SeriesListViewModel(
    editDatabase: AlleyEditDatabase,
    dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
) : ViewModel() {
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)

    val series = flowFromSuspend {
        editDatabase.loadSeries()
            .values
            .sortedBy { it.titlePreferred }
            .let { PagingData.from(it) }
    }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, PagingData.empty())

    fun loadImage(series: SeriesInfo) = imageLoader.getSeriesImage(series)
}
