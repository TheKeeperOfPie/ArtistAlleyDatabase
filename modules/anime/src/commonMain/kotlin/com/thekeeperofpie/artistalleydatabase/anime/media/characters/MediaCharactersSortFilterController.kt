package com.thekeeperofpie.artistalleydatabase.anime.media.characters

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_characters_filter_role_dropdown_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_characters_filter_role_include_exclude_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_characters_filter_role_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_characters_filter_setting_name_language
import artistalleydatabase.modules.anime.generated.resources.anime_media_characters_filter_sort_label
import com.anilist.data.type.CharacterRole
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.resources.stringResource

@OptIn(FlowPreview::class)
class MediaCharactersSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : MediaDataSettingsSortFilterController<MediaCharactersSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {
    private val sortSection = SortFilterSection.Sort(
        enumClass = CharacterSortOption::class,
        defaultEnabled = CharacterSortOption.RELEVANCE,
        headerTextRes = Res.string.anime_media_characters_filter_sort_label,
    ).apply {
        setOptions(CharacterSortOption.entries.filter { it != CharacterSortOption.SEARCH_MATCH })
    }

    private val roleSection = SortFilterSection.Filter(
        titleRes = Res.string.anime_media_characters_filter_role_label,
        titleDropdownContentDescriptionRes = Res.string.anime_media_characters_filter_role_dropdown_content_description,
        includeExcludeIconContentDescriptionRes = Res.string.anime_media_characters_filter_role_include_exclude_icon_content_description,
        values = CharacterRole.entries.filter { it != CharacterRole.UNKNOWN__ },
        valueToText = { stringResource(it.value.toTextRes()) },
        selectionMethod = SortFilterSection.Filter.SelectionMethod.ONLY_INCLUDE,
    )

    private val nameLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = Res.string.anime_media_characters_filter_setting_name_language,
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
