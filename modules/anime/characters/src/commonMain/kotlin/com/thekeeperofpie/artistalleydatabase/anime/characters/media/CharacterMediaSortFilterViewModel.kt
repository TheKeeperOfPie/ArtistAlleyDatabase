package com.thekeeperofpie.artistalleydatabase.anime.characters.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.characters.generated.resources.Res
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_media_filter_on_list_label
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_media_filter_setting_title_language
import artistalleydatabase.modules.anime.characters.generated.resources.anime_character_media_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSortOption
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
class CharacterMediaSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted initialParams: InitialParams,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    private val sortOption = savedStateHandle
        .getMutableStateFlow(json, "sortOption", MediaSortOption.TRENDING)
    private val sortAscending =
        savedStateHandle.getMutableStateFlow<Boolean>("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        defaultSort = MediaSortOption.TRENDING,
        headerText = Res.string.anime_character_media_filter_sort_label,
        sortOptions = MutableStateFlow(
            if (initialParams.allowRelevanceSort) {
                MediaSortOption.entries
            } else {
                MediaSortOption.entries.filter { it != MediaSortOption.SEARCH_MATCH }
            }
        ),
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val onList = savedStateHandle.getMutableStateFlow<Boolean?>(json, "onList", null)
    private val onListSection = SortFilterSectionState.TriStateBoolean(
        title = Res.string.anime_character_media_filter_on_list_label,
        defaultEnabled = null,
        enabled = onList,
    )

    private val titleLanguageSection = SortFilterSectionState.Dropdown(
        labelText = Res.string.anime_character_media_filter_setting_title_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = mediaDataSettings.languageOptionMedia,
    )

    private val sections = listOf(
        sortSection,
        onListSection,
        titleLanguageSection,
        makeAdvancedSection(),
    )

    private val filterParams =
        combineStates(sortOption, sortAscending, onList) { sortOption, sortAscending, onList ->
            FilterParams(
                sort = sortOption,
                sortAscending = sortAscending,
                onList = onList,
            )
        }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )

    data class InitialParams(
        val allowRelevanceSort: Boolean = false,
    )

    data class FilterParams(
        val sort: MediaSortOption,
        val sortAscending: Boolean,
        val onList: Boolean?,
    )
}
