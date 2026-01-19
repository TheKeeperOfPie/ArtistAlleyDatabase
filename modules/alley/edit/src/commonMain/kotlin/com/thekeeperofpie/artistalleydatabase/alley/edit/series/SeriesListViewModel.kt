package com.thekeeperofpie.artistalleydatabase.alley.edit.series

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.paging.PagingData
import com.thekeeperofpie.artistalleydatabase.alley.edit.data.AlleyEditDatabase
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImagesStore
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.Fixed
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class SeriesListViewModel(
    editDatabase: AlleyEditDatabase,
    dispatchers: CustomDispatchers,
    seriesImagesStore: SeriesImagesStore,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val query by savedStateHandle.saveable(saver = TextFieldState.Saver.Fixed) { TextFieldState() }
    private val imageLoader = SeriesImageLoader(dispatchers, viewModelScope, seriesImagesStore)
    private val refresh = RefreshFlow()

    private val allSeries = refresh.updates
        .mapLatest {
            editDatabase.loadSeries()
                .values
                .sortedBy { it.id }
        }
        .flowOn(dispatchers.io)

    private val debouncedQuery = snapshotFlow { query.text.toString() }.debounce(1.seconds)
    val series =
        combine(debouncedQuery, allSeries, ::Pair)
            .mapLatest { (query, series) ->
                if (query.isEmpty()) return@mapLatest series
                val (firstSection, firstRemaining) = series.partition {
                    it.id.contains(query, ignoreCase = true)
                }
                val (secondSection, secondRemaining) = firstRemaining.partition {
                    it.titlePreferred.contains(query, ignoreCase = true)
                }
                val (thirdSection, thirdRemaining) = secondRemaining.partition {
                    it.titleEnglish.contains(query, ignoreCase = true) ||
                            it.titleRomaji.contains(query, ignoreCase = true)
                }
                firstSection + secondSection + thirdSection
            }
            .mapLatest { PagingData.from(it) }
            .flowOn(dispatchers.io)
            .stateIn(viewModelScope, SharingStarted.Eagerly, PagingData.empty())

    fun refresh() = refresh.refresh()
    fun loadImage(series: SeriesInfo) = imageLoader.getSeriesImage(series)

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): SeriesListViewModel
    }
}
