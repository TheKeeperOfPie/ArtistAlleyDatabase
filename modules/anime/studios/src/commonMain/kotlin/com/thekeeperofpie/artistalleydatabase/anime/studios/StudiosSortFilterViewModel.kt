package com.thekeeperofpie.artistalleydatabase.anime.studios

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.studios.generated.resources.Res
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.filter.StudioSortOption
import com.thekeeperofpie.artistalleydatabase.anime.studios.data.filter.StudiosSortFilterParams
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class StudiosSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    private val sortOption =
        savedStateHandle.getMutableStateFlow(json, "sortOption", StudioSortOption.SEARCH_MATCH)
    private val sortAscending = savedStateHandle.getMutableStateFlow("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.anime_studio_filter_sort_label,
        defaultSort = StudioSortOption.SEARCH_MATCH,
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val sections = listOf(sortSection, makeAdvancedSection())

    private val filterParams =
        combineStates(sortOption, sortAscending) { sortOption, sortAscending ->
            StudiosSortFilterParams(sort = sortOption, sortAscending = sortAscending)
        }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): StudiosSortFilterViewModel
    }
}
