package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchCache
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchTagData
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.ui.DisplaySettings
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.Link
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.transform
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
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
    @Assisted dataYear: () -> DataYear,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted lockedSeries: () -> SeriesInfo?,
    @Assisted private val lockedMerchId: String?,
    @Assisted private val allowSettingsBasedToggles: Boolean = true,
) {
    private var localShowOnlyConfirmedTags by savedStateHandle.saveable {
        mutableStateOf(settings.showOnlyConfirmedTags)
    }

    private val merchTagData by transform(scope) {
        val dataYear = dataYear()
        produceState(MerchTagData(emptyList()), dataYear) {
            merchCache.merchTags(dataYear).collectLatest { value = it }
        }.value
    }

    private val persistentState = ArtistSortFilterPersistentState(
        sortFilterSettings = settings,
        displaySettings = object : DisplaySettings by settings {
            override var showOnlyConfirmedTags: Boolean
                get() = if (allowSettingsBasedToggles) {
                    settings.showOnlyConfirmedTags
                } else {
                    localShowOnlyConfirmedTags
                }
                set(value) = if (allowSettingsBasedToggles) {
                    settings.showOnlyConfirmedTags = value
                } else {
                    localShowOnlyConfirmedTags = value
                }
        },
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
        ArtistSortFilterParams(
            sortOption = persistentState.sortFilterSettings.artistsSortOption,
            sortAscending = persistentState.sortFilterSettings.artistsSortAscending,
            seriesIn = seriesIn,
            merchIn = saveableState.merch.tagIdIn.toSet() + setOfNotNull(lockedMerchId),
            commissionsIn = saveableState.commissions.filterIn.toSet(),
            linkTypesIn = saveableState.links.tagIdIn.toSet().map(Link.Type::valueOf).toSet(),
            exhibitorTagsIn = emptySet(), // TODO
            artistTagsIn = persistentState.sortFilterSettings.artistsTagsIn.toSet(),
            artistTagsNotIn = persistentState.sortFilterSettings.artistsTagsNotIn.toSet(),
            showOnlyConfirmedTags = persistentState.displaySettings.showOnlyConfirmedTags,
            showOutdatedCatalogs = persistentState.sortFilterSettings.showOutdatedCatalogs,
            hideFavorited = saveableState.hideFavorited,
            hideIgnored = saveableState.hideIgnored,
        )
    }

    fun clear() {
        state.clear()
    }
}
