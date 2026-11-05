package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.appstate.transform.transform
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchCache
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagData
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_app_state.rememberDebounced
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

@AssistedInject
class StampRallySortFilterController(
    dispatchers: CustomDispatchers,
    merchCache: MerchCache,
    seriesEntryDao: SeriesEntryDao,
    val settings: ArtistAlleySettings,
    @Assisted scope: CoroutineScope,
    @Assisted dataYear: () -> DataYear,
    @Assisted private val lockedSeries: () -> SeriesInfo?,
    @Assisted savedStateHandle: SavedStateHandle,
) {

    private val merchTagData by transform(MerchTagData(emptyList()), scope) {
        val dataYear = dataYear()
        produceState(MerchTagData(emptyList()), dataYear) {
            merchCache.merchTags(dataYear).collectLatest { value = it }
        }.value
    }

    private val persistentState = StampRallySortFilterPersistentState(
        sortSettings = settings,
        displaySettings = settings,
    )

    private val saveableState by savedStateHandle.saved { StampRallySortFilterSaveableState() }

    private val seriesAutocompleteResults by transform(emptyList<SeriesInfo>(), scope) {
        val query by rememberDebounced(saveableState.series.query.text.toString())
        produceState(emptyList(), query) {
            value = withContext(dispatchers.io) {
                seriesEntryDao.searchSeriesForAutocomplete(query)
            }
        }.value
    }

    val state = StampRallySortFilterState(
        persistentState = persistentState,
        saveableState = saveableState,
        lockedSeries = lockedSeries,
        merchTagData = { merchTagData },
        merchIdsLockedIn = { emptySet() },
        seriesAutocompleteResults = ::seriesAutocompleteResults,
    )

    internal val filterParams by derivedStateOf {
        val seriesIn = setOfNotNull(lockedSeries()?.id) +
                saveableState.series.seriesIn.toSet().map { it.id }
        StampRallyFilterParams(
            sortOption = persistentState.sortSettings.stampRalliesSortOption,
            sortAscending = persistentState.sortSettings.stampRalliesSortAscending,
            seriesIn = seriesIn,
            merchIdIn = saveableState.merch.tagIdIn.toSet(),
            prizeMerchIdIn = saveableState.prizeMerch.tagIdIn.toSet(),
            totalCost = saveableState.totalCost.data,
            prizeLimit = saveableState.prizeLimit.data,
            hideFavorited = saveableState.hideFavorited,
            hideIgnored = saveableState.hideIgnored,
        )
    }

    fun clear() {
        state.clear()
    }
}
