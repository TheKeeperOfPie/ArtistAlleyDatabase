package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class StaffSortFilterController(
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val allowRelevanceSort: Boolean = false,
) : SortFilterController<StaffSortFilterController.FilterParams>(settings, featureOverrideProvider) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = StaffSortOption::class,
        defaultEnabled = StaffSortOption.SEARCH_MATCH,
        headerTextRes = R.string.anime_staff_filter_sort_label,
    ).apply {
        if (!allowRelevanceSort) {
            sortOptions = sortOptions.filter { it.value != StaffSortOption.RELEVANCE }
        }
    }

    private val birthdaySection = SortFilterSection.Switch(
        titleRes = R.string.anime_staff_filter_birthday_label,
        defaultEnabled = false,
    )

    private val titleLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = R.string.anime_staff_filter_setting_title_language,
        values = AniListLanguageOption.values().toList(),
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

    override val filterParams = snapshotFlow {
        FilterParams(
            sort = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
            isBirthday = birthdaySection.enabled,
        )
    }.debounce(500.milliseconds)

    data class FilterParams(
        val sort: List<SortEntry<StaffSortOption>>,
        val sortAscending: Boolean,
        val isBirthday: Boolean,
    )
}
