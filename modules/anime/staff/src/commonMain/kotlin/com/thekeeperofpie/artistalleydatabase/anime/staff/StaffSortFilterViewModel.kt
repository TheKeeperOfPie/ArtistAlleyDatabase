package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.staff.generated.resources.Res
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_filter_birthday_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_filter_setting_title_language
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.filter.StaffSortFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.staff.data.filter.StaffSortOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@Inject
class StaffSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    staffSettings: StaffSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted initialParams: InitialParams,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = staffSettings,
) {
    private val sortOption =
        savedStateHandle.getMutableStateFlow(json, "sortOption", StaffSortOption.SEARCH_MATCH)
    private val sortAscending = savedStateHandle.getMutableStateFlow("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.anime_staff_filter_sort_label,
        defaultSort = StaffSortOption.SEARCH_MATCH,
        sortOptions = MutableStateFlow(
            if (initialParams.allowRelevanceSort) {
                StaffSortOption.entries
            } else {
                StaffSortOption.entries.filter { it != StaffSortOption.RELEVANCE }
            }
        ),
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val birthday = savedStateHandle.getMutableStateFlow("birthday", false)
    private val birthdaySection = SortFilterSectionState.Switch(
        title = Res.string.anime_staff_filter_birthday_label,
        defaultEnabled = false,
        enabled = birthday,
    )

    private val titleLanguageSection = SortFilterSectionState.Dropdown(
        labelText = Res.string.anime_staff_filter_setting_title_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = staffSettings.languageOptionStaff,
    )

    private val sections = listOf(
        sortSection,
        birthdaySection,
        titleLanguageSection,
        makeAdvancedSection(),
    )

    private val filterParams = combineStates(sortOption, sortAscending, birthday) {
        StaffSortFilterParams(
            sort = it[0] as StaffSortOption,
            sortAscending = it[1] as Boolean,
            isBirthday = it[2] as Boolean,
        )
    }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = staffSettings.collapseAnimeFiltersOnClose,
    )

    data class InitialParams(
        val allowRelevanceSort: Boolean = false,
    )
}
