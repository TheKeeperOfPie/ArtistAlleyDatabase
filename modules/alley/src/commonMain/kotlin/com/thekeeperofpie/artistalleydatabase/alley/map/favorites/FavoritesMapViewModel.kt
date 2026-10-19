package com.thekeeperofpie.artistalleydatabase.alley.map.favorites

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.data.toMerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesRowId
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.toSeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.inject.NavigatorScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.Fixed
import com.thekeeperofpie.artistalleydatabase.utils_compose.transform.transform
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

@AssistedInject
class FavoritesMapViewModel(
    applicationScope: ApplicationScope,
    artistEntryDao: ArtistEntryDao,
    dispatchers: CustomDispatchers,
    val settings: ArtistAlleySettings,
    merchEntryDao: MerchEntryDao,
    seriesEntryDao: SeriesEntryDao,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val tagAutocomplete = TagAutocomplete(
        applicationScope = applicationScope,
        dispatchers = dispatchers,
        loadSeries = { seriesEntryDao.getSeries().map { it.toSeriesInfo() }.associateBy { it.id } },
        loadMerch = { merchEntryDao.getMerch().map { it.toMerchInfo() }.associateBy { it.name } },
    )

    val query by savedStateHandle.saveable(saver = TextFieldState.Saver.Fixed, { TextFieldState() })

    var seriesRowIdIn by savedStateHandle.saveable { mutableStateOf(emptyList<SeriesRowId>()) }
    var merchIdIn by savedStateHandle.saveable { mutableStateOf(emptyList<String>()) }

    val tagResults by transform(viewModelScope) {
        val query = query.text.toString()
        produceState(emptyList<MerchInfo>() to emptyList(), query) {
            if (query.isNotEmpty()) {
                combine(
                    tagAutocomplete.merchPredictions(query, allowCustom = false),
                    tagAutocomplete.seriesPredictions(query, allowCustom = false),
                    ::Pair,
                ).collectLatest { value = it }
            }
        }.value
    }

    val highlightedBooths by transform(viewModelScope) {
        val seriesRowIdIn = seriesRowIdIn
        val merchIdIn = merchIdIn
        if (seriesRowIdIn.isEmpty() && merchIdIn.isEmpty()) {
            return@transform emptySet()
        }
        val dataYear by settings.dataYear.collectAsStateWithLifecycle()
        val showOnlyConfirmedTags = settings.showOnlyConfirmedTags
        produceState(emptySet(), dataYear, showOnlyConfirmedTags, seriesRowIdIn, merchIdIn) {
            value = artistEntryDao.searchBooths(
                year = dataYear,
                seriesIds = seriesRowIdIn.toSet(),
                merchIds = merchIdIn.toSet(),
                showOnlyConfirmedTags = showOnlyConfirmedTags,
            )
        }.value
    }

    val filterState = FavoritesMapFilterState(
        dataYear = { settings.dataYear.collectAsStateWithLifecycle().value },
        onDataYearChange = { settings.dataYear.value = it },
        showOnlyConfirmedTags = { settings.showOnlyConfirmedTags },
        onShowOnlyConfirmedTagsChanged = { settings.showOnlyConfirmedTags = it },
        showRandomCatalogImage = { settings.showRandomCatalogImage },
        onShowRandomCatalogImageChanged = { settings.showRandomCatalogImage = it },
    )

    fun onSeriesSelected(seriesInfo: SeriesInfo, selected: Boolean) = Snapshot.withMutableSnapshot {
        val id = seriesInfo.rowid
        seriesRowIdIn = if (selected) {
            if (id in seriesRowIdIn) {
                seriesRowIdIn
            } else {
                seriesRowIdIn + id
            }
        } else {
            seriesRowIdIn - id
        }
    }

    fun onMerchSelected(merchInfo: MerchInfo, selected: Boolean) = Snapshot.withMutableSnapshot {
        val id = merchInfo.name
        merchIdIn = if (selected) {
            if (id in merchIdIn) {
                merchIdIn
            } else {
                merchIdIn + id
            }
        } else {
            merchIdIn - id
        }
    }

    fun onClearTags() {
        seriesRowIdIn = emptyList()
        merchIdIn = emptyList()
    }

    @AssistedFactory
    @ManualViewModelAssistedFactoryKey
    @ContributesIntoMap(NavigatorScope::class)
    interface Factory : ManualViewModelAssistedFactory {
        fun create(savedStateHandle: SavedStateHandle): FavoritesMapViewModel
    }
}
