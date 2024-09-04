package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.AnimeSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeResourceUtils
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class StaffSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val allowRelevanceSort: Boolean = false,
) : AnimeSettingsSortFilterController<StaffSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = StaffSortOption::class,
        defaultEnabled = StaffSortOption.SEARCH_MATCH,
        headerTextRes = R.string.anime_staff_filter_sort_label,
    ).apply {
        if (!allowRelevanceSort) {
            setOptions(StaffSortOption.entries.filter { it != StaffSortOption.RELEVANCE })
        }
    }

    private val birthdaySection = SortFilterSection.Switch(
        titleRes = R.string.anime_staff_filter_birthday_label,
        defaultEnabled = false,
    )

    private val titleLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = R.string.anime_staff_filter_setting_title_language,
        values = AniListLanguageOption.values().toList(),
        valueToText = { ComposeResourceUtils.stringResource(it.textRes) },
        property = settings.languageOptionStaff,
    )

    override var sections = listOf(
        sortSection,
        birthdaySection,
        titleLanguageSection,
        advancedSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
        isBirthday = birthdaySection.enabled,
    )

    data class FilterParams(
        val sort: List<SortEntry<StaffSortOption>>,
        val sortAscending: Boolean,
        val isBirthday: Boolean,
    )
}
