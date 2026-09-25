package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchCache
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagData
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.transform
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.milliseconds

@AssistedInject
class StampRallySortFilterController(
    dispatchers: CustomDispatchers,
    merchCache: MerchCache,
    seriesEntryDao: SeriesEntryDao,
    val settings: ArtistAlleySettings,
    @Assisted scope: CoroutineScope,
    @Assisted dataYear: StateFlow<DataYear>,
    @Assisted lockedSeriesEntry: StateFlow<SeriesInfo?>,
    @Assisted savedStateHandle: SavedStateHandle,
) {

    private val merchTagData by transform(scope) {
        produceState(MerchTagData(emptyList())) {
            dataYear.flatMapLatest(merchCache::merchTags)
                .collectLatest { value = it }
        }.value
    }

    private val lockedSeries by transform(scope) {
        lockedSeriesEntry.collectAsState().value
    }

    private val persistentState = StampRallySortFilterPersistentState(
        sortOption = settings.stampRalliesSortOption,
        sortAscending = settings.stampRalliesSortAscending,
        showGridByDefault = settings.showGridByDefault,
        showRandomCatalogImage = settings.showRandomCatalogImage,
        forceOneDisplayColumn = settings.forceOneDisplayColumn,
    )

    private val saveableState by savedStateHandle.saved { StampRallySortFilterSaveableState() }

    val state = StampRallySortFilterState(
        persistentState = persistentState,
        saveableState = saveableState,
        lockedSeries = { lockedSeries },
        merchTagData = { merchTagData },
        merchIdsLockedIn = { emptySet() },
    )

    val seriesAutocompleteResults =
        snapshotFlow { state.saveableState.series.query.text.toString() }
            .debounce(500.milliseconds)
            .mapLatest(seriesEntryDao::searchSeriesForAutocomplete)
            .flowOn(dispatchers.io)
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    internal val filterParams by transform(scope) {
        val sortOption by persistentState.sortOption.collectAsState()
        val sortAscending by persistentState.sortAscending.collectAsState()
        val seriesIn = setOfNotNull(lockedSeriesEntry.collectAsState().value?.id) +
                saveableState.series.seriesIn.toSet().map { it.id }
        StampRallyFilterParams(
            sortOption = sortOption,
            sortAscending = sortAscending,
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
