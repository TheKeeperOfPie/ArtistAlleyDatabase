package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_character_filter_birthday_label
import artistalleydatabase.modules.anime.generated.resources.anime_character_filter_setting_name_language
import artistalleydatabase.modules.anime.generated.resources.anime_character_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

class CharacterSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val allowRelevanceSort: Boolean = false,
) : MediaDataSettingsSortFilterController<CharacterSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = CharacterSortOption::class,
        defaultEnabled = CharacterSortOption.SEARCH_MATCH,
        headerTextRes = Res.string.anime_character_filter_sort_label,
    ).apply {
        if (!allowRelevanceSort) {
            setOptions(CharacterSortOption.entries.filter { it != CharacterSortOption.RELEVANCE })
        }
    }

    private val birthdaySection = SortFilterSection.Switch(
        titleRes = Res.string.anime_character_filter_birthday_label,
        defaultEnabled = false,
    )

    private val nameLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = Res.string.anime_character_filter_setting_name_language,
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
