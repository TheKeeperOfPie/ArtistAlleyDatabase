package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.alley.database.UserEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryCache
import com.thekeeperofpie.artistalleydatabase.alley.tags.SeriesImageLoader
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.getOrPut
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.math.absoluteValue
import kotlin.random.Random

@AssistedInject
class StampRallyChangelogViewModel(
    stampRallyEntryDao: StampRallyEntryDao,
    dispatchers: CustomDispatchers,
    val seriesEntryCache: SeriesEntryCache,
    private val seriesImageLoader: SeriesImageLoader,
    userEntryDao: UserEntryDao,
    @Assisted dataYear: DataYear,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val randomSeed =
        savedStateHandle.getOrPut("randomSeed") { Random.nextInt().absoluteValue }

    internal val changes = combine(
        userEntryDao.getTagFavorites(),
        flowFromSuspend { stampRallyEntryDao.getAllEntriesForChangelog(dataYear) },
        ::Pair
    )
        .mapLatest { (tagFavorites, allRallies) ->
            stampRallyEntryDao.getChangelog(dataYear)
                .mapNotNull {
                    it.toChangelogEntry(
                        dataYear = dataYear,
                        allRallies = allRallies,
                        randomSeed = randomSeed,
                        seriesIdsToHighlight = tagFavorites.seriesIds,
                        merchIdsToHighlight = tagFavorites.merchIds,
                    )
                }
        }
        .mapLatest {
            it.groupBy { it.date }
                .toList()
                .sortedByDescending { it.first }
                .map {
                    val (added, updated) = it.second.partition { it.isBrandNew }
                    StampRallyChangelogScreen.DayChange(
                        date = it.first,
                        added = added.sortRalliesForChangelog(),
                        updated = updated.sortRalliesForChangelog(),
                    )
                }
        }
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun seriesImage(seriesId: String) = seriesImageLoader.getSeriesImage(seriesId)
}
