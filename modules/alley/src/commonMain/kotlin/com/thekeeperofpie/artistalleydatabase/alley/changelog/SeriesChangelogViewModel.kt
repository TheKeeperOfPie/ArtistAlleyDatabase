package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.LocalDate

@AssistedInject
class SeriesChangelogViewModel(
    val seriesEntryCache: SeriesEntryCache,
    seriesEntryDao: SeriesEntryDao,
    private val seriesImageLoader: SeriesImageLoader,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val changes = flowFromSuspend {
        seriesEntryDao.getSeriesChangelog()
    }.combine(seriesEntryCache.series, ::Pair)
        .mapLatest { (changelog, seriesTitles) ->

            changelog.sortedByDescending { it.date }
                .map {
                    SeriesChangelogScreen.DayChange(
                        date = LocalDate.parse(it.date),
                        seriesIds = it.seriesIds?.sorted().orEmpty().mapNotNull {
                            seriesTitles[it]
                        },
                    )
                }
        }
        .stateInForCompose(emptyList())

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
