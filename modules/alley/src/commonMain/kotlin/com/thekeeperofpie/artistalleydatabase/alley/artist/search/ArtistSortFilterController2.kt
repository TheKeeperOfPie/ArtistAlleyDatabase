package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchCache
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagData
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.transform
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest

@Inject
class ArtistSortFilterController2(
    scope: CoroutineScope,
    settings: ArtistAlleySettings,
    savedStateHandle: SavedStateHandle,
    dataYear: StateFlow<DataYear>,
    lockedSeriesEntry: StateFlow<SeriesInfo?>,
    lockedMerchId: String?,
    merchCache: MerchCache,
    allowSettingsBasedToggles: Boolean = true,
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

    private val lockedSeries by transform(scope) {
        lockedSeriesEntry.collectAsState().value
    }

    private val persistentState = ArtistSortFilterPersistentState(
        sortOption = settings.artistsSortOption,
        sortAscending = settings.artistsSortAscending,
        artistTagsIn = settings.artistTagsIn,
        artistTagsNotIn = settings.artistTagsNotIn,
        showGridByDefault = settings.showGridByDefault,
        showRandomCatalogImage = settings.showRandomCatalogImage,
        showOnlyConfirmedTags = showOnlyConfirmedTags,
        showOutdatedCatalogs = settings.showOutdatedCatalogs,
        forceOneDisplayColumn = settings.forceOneDisplayColumn,
    )

    private val saveableState by savedStateHandle.saved { ArtistSortFilterSaveableState() }

    val state = ArtistSortFilterState(
        persistentState = persistentState,
        saveableState = saveableState,
        lockedSeries = { lockedSeries },
        merchTagData = { merchTagData },
        merchIdsLockedIn = { setOfNotNull(lockedMerchId) },
    )

    fun clear() {
        state.clear()
    }
}
