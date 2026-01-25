package com.thekeeperofpie.artistalleydatabase.alley.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.series.toImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@AssistedInject
class ArtistSeriesViewModel(
    dispatchers: CustomDispatchers,
    seriesEntryDao: SeriesEntryDao,
    seriesImagesStore: SeriesImagesStore,
    userEntryDao: UserEntryDao,
    @Assisted series: String,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val seriesEntry = seriesEntryDao.getSeriesByIdWithUserData(series)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val seriesImage = seriesEntry.filterNotNull()
        .map { it.series }
        .map {
            val cachedResult = seriesImagesStore.getCachedImages(listOf(it.toImageInfo()))
            val cachedImage = cachedResult.seriesIdsToImages[it.id]
            if (cachedImage != null) return@map cachedImage
            seriesImagesStore.getAllImages(listOf(it.toImageInfo()), cachedResult)[it.id]
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val mutationUpdates = MutableSharedFlow<SeriesUserEntry>(5, 5)

    init {
        viewModelScope.launch(dispatchers.io) {
            mutationUpdates.collectLatest {
                userEntryDao.insertSeriesUserEntry(it)
            }
        }
    }

    fun onFavoriteToggle(data: SeriesWithUserData, favorite: Boolean) {
        mutationUpdates.tryEmit(data.userEntry.copy(favorite = favorite))
    }

    @AssistedFactory
    interface Factory {
        fun create(series: String, savedStateHandle: SavedStateHandle): ArtistSeriesViewModel
    }
}
