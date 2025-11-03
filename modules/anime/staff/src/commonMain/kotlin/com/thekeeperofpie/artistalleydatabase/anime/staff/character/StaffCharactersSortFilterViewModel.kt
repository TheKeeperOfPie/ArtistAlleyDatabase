package com.thekeeperofpie.artistalleydatabase.anime.staff.character

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.staff.generated.resources.Res
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_characters_filter_setting_name_language
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_characters_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSettings
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class StaffCharactersSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    staffSettings: StaffSettings,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    private val sortOption =
        savedStateHandle.getMutableStateFlow(json, "sortOption", CharacterSortOption.FAVORITES)
    private val sortAscending = savedStateHandle.getMutableStateFlow("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.anime_staff_characters_filter_sort_label,
        defaultSort = CharacterSortOption.FAVORITES,
        sortOptions = MutableStateFlow(
            CharacterSortOption.entries
                .filter { it != CharacterSortOption.SEARCH_MATCH && it != CharacterSortOption.RELEVANCE }
        ),
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val nameLanguageSection = SortFilterSectionState.Dropdown(
        labelText = Res.string.anime_staff_characters_filter_setting_name_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = staffSettings.languageOptionCharacters,
    )

    private val sections = listOf(
        sortSection,
        nameLanguageSection,
        makeAdvancedSection(),
    )

    private val filterParams =
        combineStates(sortOption, sortAscending) { sortOption, sortAscending ->
            FilterParams(sort = sortOption, sortAscending = sortAscending)
        }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )

    data class FilterParams(
        val sort: CharacterSortOption,
        val sortAscending: Boolean,
    )

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): StaffCharactersSortFilterViewModel
    }
}
