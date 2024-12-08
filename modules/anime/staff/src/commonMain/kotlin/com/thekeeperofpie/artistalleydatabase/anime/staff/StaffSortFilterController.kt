package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.staff.generated.resources.Res
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_filter_birthday_label
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_filter_setting_title_language
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.resources.stringResource

@OptIn(FlowPreview::class)
class StaffSortFilterController(
    scope: CoroutineScope,
    settings: StaffSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val allowRelevanceSort: Boolean = false,
) : MediaDataSettingsSortFilterController<StaffSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = StaffSortOption::class,
        defaultEnabled = StaffSortOption.SEARCH_MATCH,
        headerTextRes = Res.string.anime_staff_filter_sort_label,
    ).apply {
        if (!allowRelevanceSort) {
            setOptions(StaffSortOption.entries.filter { it != StaffSortOption.RELEVANCE })
        }
    }

    private val birthdaySection = SortFilterSection.Switch(
        titleRes = Res.string.anime_staff_filter_birthday_label,
        defaultEnabled = false,
    )

    private val titleLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = Res.string.anime_staff_filter_setting_title_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
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
