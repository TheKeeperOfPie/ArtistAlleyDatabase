package com.thekeeperofpie.artistalleydatabase.anime.media.characters

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anilist.type.CharacterRole
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.filter.AnimeSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.FeatureOverrideProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class MediaCharactersSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : AnimeSettingsSortFilterController<MediaCharactersSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {
    private val sortSection = SortFilterSection.Sort(
        enumClass = CharacterSortOption::class,
        defaultEnabled = CharacterSortOption.RELEVANCE,
        headerTextRes = R.string.anime_media_characters_filter_sort_label,
    ).apply {
        setOptions(CharacterSortOption.entries.filter { it != CharacterSortOption.SEARCH_MATCH })
    }

    private val roleSection = SortFilterSection.Filter(
        titleRes = R.string.anime_media_characters_filter_role_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_characters_filter_role_dropdown_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_media_characters_filter_role_include_exclude_icon_content_description,
        values = CharacterRole.entries.filter { it != CharacterRole.UNKNOWN__ },
        valueToText = { stringResource(it.value.toTextRes()) },
        selectionMethod = SortFilterSection.Filter.SelectionMethod.ONLY_INCLUDE,
    )

    private val nameLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = R.string.anime_media_characters_filter_setting_name_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = settings.languageOptionCharacters,
    )

    override var sections = listOf(
        sortSection,
        roleSection,
        nameLanguageSection,
        advancedSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
        role = roleSection.filterOptions
            .find { it.state == FilterIncludeExcludeState.INCLUDE }
            ?.value,
    )

    data class FilterParams(
        val sort: List<SortEntry<CharacterSortOption>>,
        val sortAscending: Boolean,
        val role: CharacterRole?,
    )
}
