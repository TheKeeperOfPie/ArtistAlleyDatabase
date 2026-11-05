package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.appstate.transform.transform
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thekeeperofpie.artistalleydatabase.alley.data.SeriesEntryChangelog
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.datetime.LocalDate

@AssistedInject
class SeriesChangelogViewModel(
    val seriesEntryCache: SeriesEntryCache,
    seriesEntryDao: SeriesEntryDao,
    private val seriesImageLoader: SeriesImageLoader,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val changes by transform(emptyList<SeriesChangelogScreen.DayChange>(), viewModelScope) {
        val seriesChangelog by produceState<List<SeriesEntryChangelog>>(emptyList()) {
            value = seriesEntryDao.getSeriesChangelog()
        }
        val seriesTitles = seriesEntryCache.series
        seriesChangelog.sortedByDescending { it.date }
            .map {
                SeriesChangelogScreen.DayChange(
                    date = LocalDate.parse(it.date),
                    seriesIds = it.seriesIds?.sorted().orEmpty().mapNotNull {
                        seriesTitles[it]
                    },
                )
            }
    }

    fun seriesImage(seriesId: String) = seriesImageLoader.getSeriesImage(seriesId)

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(
            savedStateHandle: SavedStateHandle,
        ): SeriesChangelogViewModel
    }
}
