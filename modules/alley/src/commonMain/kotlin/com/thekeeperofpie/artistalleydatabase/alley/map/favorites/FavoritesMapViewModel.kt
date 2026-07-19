package com.thekeeperofpie.artistalleydatabase.alley.map.favorites

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_filter_data_year
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_only_confirmed_tags
import artistalleydatabase.modules.alley.generated.resources.alley_filter_show_random_catalog_image
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.data.toMerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.merch.MerchEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.models.MerchInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.models.SeriesRowId
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.series.toSeriesInfo
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.shortName
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagAutocomplete
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.stateInForCompose
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.stringResource

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

    val query =
        savedStateHandle.saveable(key = "query", saver = TextFieldState.Saver, { TextFieldState() })

    val seriesRowIdIn = savedStateHandle.getMutableStateFlow("seriesIdIn", emptyList<SeriesRowId>())
    val merchIdIn = savedStateHandle.getMutableStateFlow("merchIdIn", emptyList<String>())

    val tagResults = combine(settings.dataYear, snapshotFlow { query.text.toString() }, ::Pair)
        .flatMapLatest { (dataYear, query) ->
            if (query.isEmpty()) {
                flowOf(emptyList<MerchInfo>() to emptyList())
            } else {
                combine(
                    tagAutocomplete.merchPredictions(query, allowCustom = false),
                    tagAutocomplete.seriesPredictions(query, allowCustom = false),
                    ::Pair,
                )
            }
        }
        .flowOn(dispatchers.io)
        .stateInForCompose(emptyList<MerchInfo>() to emptyList())

    val highlightedBooths = combine(
        settings.dataYear,
        settings.showOnlyConfirmedTags,
        seriesRowIdIn,
        merchIdIn,
        ::TagInput,
    ).mapLatest {
        if (it.seriesIdIn.isEmpty() && it.merchIdIn.isEmpty()) {
            return@mapLatest emptySet()
        }
        artistEntryDao.searchBooths(
            year = it.dataYear,
            seriesIds = it.seriesIdIn.toSet(),
            merchIds = it.merchIdIn.toSet(),
            showOnlyConfirmedTags = it.showOnlyConfirmedTags,
        )
    }.flowOn(dispatchers.io)
        .stateInForCompose(emptySet())

    private val randomCatalogImageSection = SortFilterSectionState.SwitchBySetting(
        Res.string.alley_filter_show_random_catalog_image,
        settings.showRandomCatalogImage,
    )

    private val dataYearSection = SortFilterSectionState.Dropdown(
        labelText = Res.string.alley_filter_data_year,
        values = DataYear.entries,
        valueToText = { stringResource(it.shortName) },
        property = settings.dataYear,
    )

    val showOnlyConfirmedTagsSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.alley_filter_show_only_confirmed_tags,
        property = settings.showOnlyConfirmedTags,
        default = false,
        allowClear = true,
    )

    private val sections = listOf(
        dataYearSection,
        randomCatalogImageSection,
        showOnlyConfirmedTagsSection,
    )

    private val filterParams = settings.showRandomCatalogImage
        .mapState(viewModelScope) {
            FilterParams(
                showRandomCatalogImage = it,
            )
        }

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = ReadOnlyStateFlow(false),
    )

    fun onSeriesSelected(seriesInfo: SeriesInfo, selected: Boolean) =
        seriesRowIdIn.update {
            val id = seriesInfo.rowid
            if (selected) {
                if (id in it) {
                    it
                } else {
                    it + id
                }
            } else {
                it - id
            }
        }

    fun onMerchSelected(merchInfo: MerchInfo, selected: Boolean) =
        merchIdIn.update {
            val id = merchInfo.name
            if (selected) {
                if (id in it) {
                    it
                } else {
                    it + id
                }
            } else {
                it - id
            }
        }

    fun onClearTags() {
        seriesRowIdIn.value = emptyList()
        merchIdIn.value = emptyList()
    }

    private data class TagInput(
        val dataYear: DataYear,
        val showOnlyConfirmedTags: Boolean,
        val seriesIdIn: List<SeriesRowId>,
        val merchIdIn: List<String>,
    )

    data class FilterParams(
        val showRandomCatalogImage: Boolean,
    )

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): FavoritesMapViewModel
    }
}
