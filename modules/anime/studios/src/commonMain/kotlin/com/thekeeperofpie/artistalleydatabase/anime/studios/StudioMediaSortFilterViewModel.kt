package com.thekeeperofpie.artistalleydatabase.anime.studios

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.studios.generated.resources.Res
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_media_filter_main_label
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_media_filter_setting_title_language
import artistalleydatabase.modules.anime.studios.generated.resources.anime_studio_media_filter_sort_label
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
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class StudioMediaSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    private val sortOption =
        savedStateHandle.getMutableStateFlow(json, "sortOption", MediaSortOption.TRENDING)
    private val sortAscending = savedStateHandle.getMutableStateFlow("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.anime_studio_media_filter_sort_label,
        defaultSort = MediaSortOption.TRENDING,
        sortOptions = MutableStateFlow(
            MediaSortOption.entries.filter { it != MediaSortOption.SEARCH_MATCH }
        ),
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val main = savedStateHandle.getMutableStateFlow<Boolean?>(json, "main", null)
    private val mainSection = SortFilterSectionState.TriStateBoolean(
        title = Res.string.anime_studio_media_filter_main_label,
        defaultEnabled = false,
        enabled = main,
    )

    private val titleLanguageSection = SortFilterSectionState.Dropdown(
        labelText = Res.string.anime_studio_media_filter_setting_title_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = mediaDataSettings.languageOptionMedia,
    )

    private val sections = listOf(
        sortSection,
        mainSection,
        titleLanguageSection,
        makeAdvancedSection(),
    )

    private val filterParams = combineStates(sortOption, sortAscending, main) {
        FilterParams(
            sort = it[0] as MediaSortOption,
            sortAscending = it[1] as Boolean,
            main = it[2] as Boolean?,
        )
    }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )

    data class FilterParams(
        val sort: MediaSortOption,
        val sortAscending: Boolean,
        val main: Boolean?,
    )

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): StudioMediaSortFilterViewModel
    }
}
