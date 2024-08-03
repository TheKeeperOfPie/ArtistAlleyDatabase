package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.AnimeSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class CharacterSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val allowRelevanceSort: Boolean = false,
) : AnimeSettingsSortFilterController<CharacterSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = CharacterSortOption::class,
        defaultEnabled = CharacterSortOption.SEARCH_MATCH,
        headerTextRes = R.string.anime_character_filter_sort_label,
    ).apply {
        if (!allowRelevanceSort) {
            setOptions(CharacterSortOption.entries.filter { it != CharacterSortOption.RELEVANCE })
        }
    }

    private val birthdaySection = SortFilterSection.Switch(
        titleRes = R.string.anime_character_filter_birthday_label,
        defaultEnabled = false,
    )

    private val nameLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = R.string.anime_character_filter_setting_name_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = settings.languageOptionCharacters,
    )

    override var sections = listOf(
        sortSection,
        birthdaySection,
        nameLanguageSection,
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
        val sort: List<SortEntry<CharacterSortOption>>,
        val sortAscending: Boolean,
        val isBirthday: Boolean,
    )
}
