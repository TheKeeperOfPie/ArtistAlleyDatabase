package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ArtistSeriesViewModel(
    navigationTypeMap: NavigationTypeMap,
    seriesEntryDao: SeriesEntryDao,
    seriesImagesStore: SeriesImagesStore,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Serializable
    data class InternalRoute(
        val series: String? = null,
    )

    val route = savedStateHandle.toDestination<InternalRoute>(navigationTypeMap)

    val seriesEntry = if (route.series == null) {
        ReadOnlyStateFlow(null)
    } else {
        flowFromSuspend { seriesEntryDao.getSeriesById(route.series) }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

    val seriesImage = if (route.series == null) {
        ReadOnlyStateFlow(null)
    } else {
        seriesEntry.filterNotNull()
            .map {
                val cachedResult = seriesImagesStore.getCachedImages(listOf(it))
                val cachedImage = cachedResult.seriesIdsToImages[it.id]
                if (cachedImage != null) return@map cachedImage
                seriesImagesStore.getAllImages(listOf(it), cachedResult)[it.id]
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
    }
}
