package com.thekeeperofpie.artistalleydatabase.alley.metrics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
@AssistedInject
class MetricsViewModel(
    settings: ArtistAlleySettings,
    private val metricsDao: MetricsDao,
    val seriesEntryCache: SeriesEntryCache,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val dataYear = MutableStateFlow(settings.dataYear.value)
    val includeInferred = MutableStateFlow(false)

    internal val data = combine(dataYear, includeInferred, ::Pair)
        .mapLatest { (year, includeInferred) ->
            Data(
                artistsBySeries = metricsDao.getArtistsWithMostSeries(year, includeInferred),
                artistsByMerch = metricsDao.getArtistsWithMostMerch(year, includeInferred),
                artistsByRallies = metricsDao.getArtistsWithMostRallies(year),
                series = metricsDao.getPopularSeries(year, includeInferred),
                merch = metricsDao.getPopularMerch(year, includeInferred),
            )
        }
        .stateInForCompose(Data())

    data class Data(
        val artistsBySeries: List<MetricsDao.ArtistData> = emptyList(),
        val artistsByMerch: List<MetricsDao.ArtistData> = emptyList(),
        val artistsByRallies: List<MetricsDao.ArtistData> = emptyList(),
        val series: List<MetricsDao.SeriesData> = emptyList(),
        val merch: List<MetricsDao.MerchData> = emptyList(),
    )

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(savedStateHandle: SavedStateHandle): MetricsViewModel
    }
}
