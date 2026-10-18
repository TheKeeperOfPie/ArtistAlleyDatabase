package com.thekeeperofpie.artistalleydatabase.alley.artist.search

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
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
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
class ArtistSortFilterController(
    settings: ArtistAlleySettings,
    dispatchers: CustomDispatchers,
    merchCache: MerchCache,
    seriesEntryDao: SeriesEntryDao,
    @Assisted scope: CoroutineScope,
    @Assisted dataYear: StateFlow<DataYear>,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted lockedSeries: () -> SeriesInfo?,
    @Assisted private val lockedMerchId: String?,
    @Assisted allowSettingsBasedToggles: Boolean = true,
) {
    val showOnlyConfirmedTags = if (allowSettingsBasedToggles) {
        settings.showOnlyConfirmedTags
    } else {
        savedStateHandle.getMutableStateFlow(
            key = "showOnlyConfirmedTags",
            initialValue = settings.showOnlyConfirmedTags.value,
        )
    }

    private val merchTagData by transform(scope) {
        produceState(MerchTagData(emptyList())) {
            dataYear.flatMapLatest(merchCache::merchTags)
                .collectLatest { value = it }
        }.value
    }

    private val persistentState = ArtistSortFilterPersistentState(
        sortFilterSettings = settings,
        showGridByDefault = settings.showGridByDefault,
        showRandomCatalogImage = settings.showRandomCatalogImage,
        showOnlyConfirmedTags = showOnlyConfirmedTags,
        forceOneDisplayColumn = settings.forceOneDisplayColumn,
    )

    private val saveableState by savedStateHandle.saved { ArtistSortFilterSaveableState() }

    val state = ArtistSortFilterState(
        persistentState = persistentState,
        saveableState = saveableState,
        lockedSeries = lockedSeries,
        merchTagData = { merchTagData },
        merchIdsLockedIn = { setOfNotNull(lockedMerchId) },
    )

    val seriesAutocompleteResults =
        snapshotFlow { state.saveableState.series.query.text.toString() }
            .debounce(500.milliseconds)
            .mapLatest(seriesEntryDao::searchSeriesForAutocomplete)
            .flowOn(dispatchers.io)
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    internal val filterParams by transform(scope) {
        val seriesIn = setOfNotNull(lockedSeries()?.rowid) +
                saveableState.series.seriesIn.toSet().map { it.rowid }
        val showOnlyConfirmedTags by persistentState.showOnlyConfirmedTags.collectAsState()
        ArtistSortFilterParams(
            sortOption = persistentState.sortFilterSettings.artistsSortOption,
            sortAscending = persistentState.sortFilterSettings.artistsSortAscending,
            seriesIn = seriesIn,
            merchIn = saveableState.merch.tagIdIn.toSet() + setOfNotNull(lockedMerchId),
            commissionsIn = saveableState.commissions.filterIn.toSet(),
            linkTypesIn = saveableState.links.tagIdIn.toSet().map(Link.Type::valueOf).toSet(),
            exhibitorTagsIn = emptySet(), // TODO
            artistTagsIn = persistentState.sortFilterSettings.artistsTagsIn,
            artistTagsNotIn = persistentState.sortFilterSettings.artistsTagsNotIn,
            showOnlyConfirmedTags = showOnlyConfirmedTags,
            showOutdatedCatalogs = persistentState.sortFilterSettings.showOutdatedCatalogs,
            hideFavorited = saveableState.hideFavorited,
            hideIgnored = saveableState.hideIgnored,
        )
    }

    fun clear() {
        state.clear()
    }
}
