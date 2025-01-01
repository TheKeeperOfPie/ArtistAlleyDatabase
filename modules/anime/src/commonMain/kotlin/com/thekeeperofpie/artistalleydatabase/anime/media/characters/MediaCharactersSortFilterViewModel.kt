package com.thekeeperofpie.artistalleydatabase.anime.media.characters

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_characters_filter_role_dropdown_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_characters_filter_role_include_exclude_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_characters_filter_role_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_characters_filter_setting_name_language
import artistalleydatabase.modules.anime.generated.resources.anime_media_characters_filter_sort_label
import com.anilist.data.type.CharacterRole
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterSettings
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterSortOption
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ReadOnlyStateFlow
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
class MediaCharactersSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    characterSettings: CharacterSettings,
    mediaDataSettings: MediaDataSettings,
    @Assisted val savedStateHandle: SavedStateHandle,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    private val sortOption =
        savedStateHandle.getMutableStateFlow("sortOption", CharacterSortOption.RELEVANCE)
    private val sortAscending = savedStateHandle.getMutableStateFlow("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        enumClass = CharacterSortOption::class,
        headerText = Res.string.anime_media_characters_filter_sort_label,
        defaultSort = CharacterSortOption.RELEVANCE,
        sortOptions = MutableStateFlow(
            CharacterSortOption.entries.filter { it != CharacterSortOption.SEARCH_MATCH }
        ),
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val roleIn =
        savedStateHandle.getMutableStateFlow<Set<CharacterRole>>(json, "roleIn", emptySet())
    private val roleSection = SortFilterSectionState.Filter(
        title = Res.string.anime_media_characters_filter_role_label,
        titleDropdownContentDescription = Res.string.anime_media_characters_filter_role_dropdown_content_description,
        includeExcludeIconContentDescription = Res.string.anime_media_characters_filter_role_include_exclude_icon_content_description,
        options = ReadOnlyStateFlow(CharacterRole.entries.filter { it != CharacterRole.UNKNOWN__ }),
        filterIn = roleIn,
        filterNotIn = MutableStateFlow(emptySet()),
        valueToText = { stringResource(it.toTextRes()) },
        selectionMethod = SortFilterSectionState.Filter.SelectionMethod.AT_MOST_ONE,
    )

    private val nameLanguageSection = SortFilterSectionState.Dropdown(
        labelTextRes = Res.string.anime_media_characters_filter_setting_name_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = characterSettings.languageOptionCharacters,
    )

    private val sections = listOf(
        sortSection,
        roleSection,
        nameLanguageSection,
        makeAdvancedSection(),
    )

    @Suppress("UNCHECKED_CAST")
    private val filterParams = combineStates(sortOption, sortAscending, roleIn) {
        FilterParams(
            sort = it[0] as CharacterSortOption,
            sortAscending = it[1] as Boolean,
            role = (it[2] as Set<CharacterRole>).firstOrNull()
        )
    }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )

    data class FilterParams(
        val sort: CharacterSortOption,
        val sortAscending: Boolean,
        val role: CharacterRole?,
    )
}
