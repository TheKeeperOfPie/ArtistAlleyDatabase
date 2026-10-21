package com.thekeeperofpie.artistalleydatabase.alley.metrics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.transform
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey

@AssistedInject
class MetricsViewModel(
    private val settings: ArtistAlleySettings,
    private val metricsDao: MetricsDao,
    val seriesEntryCache: SeriesEntryCache,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var dataYear: DataYear
        get() = settings.dataYear
        set(value) { settings.dataYear = value }

    var includeInferred by savedStateHandle.saveable { mutableStateOf(false) }

    internal val data by transform(viewModelScope) {
        val year = dataYear
        val includeInferred = includeInferred
        produceState(Data(), year, includeInferred) {
            value = Data()
            value = Data(
                artistsBySeries = metricsDao.getArtistsWithMostSeries(year, includeInferred),
                artistsByMerch = metricsDao.getArtistsWithMostMerch(year, includeInferred),
                artistsByRallies = metricsDao.getArtistsWithMostRallies(year),
                series = metricsDao.getPopularSeries(year, includeInferred),
                merch = metricsDao.getPopularMerch(year, includeInferred),
            )
        }.value
    }

    data class Data(
        val artistsBySeries: List<MetricsDao.ArtistData>? = null,
        val artistsByMerch: List<MetricsDao.ArtistData>? = null,
        val artistsByRallies: List<MetricsDao.ArtistData>? = null,
        val series: List<MetricsDao.SeriesData>? = null,
        val merch: List<MetricsDao.MerchData>? = null,
    )

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(savedStateHandle: SavedStateHandle): MetricsViewModel
    }
}
