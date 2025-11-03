package com.thekeeperofpie.artistalleydatabase.anime.recommendations.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.recommendations.generated.resources.Res
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_media_recommendations_filter_setting_title_language
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_media_recommendations_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.recommendations.RecommendationSortOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class MediaRecommendationsSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    private val sortOption =
        savedStateHandle.getMutableStateFlow(json, "sortOption", RecommendationSortOption.RATING)
    private val sortAscending = savedStateHandle.getMutableStateFlow("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        defaultSort = RecommendationSortOption.RATING,
        headerText = Res.string.anime_media_recommendations_filter_sort_label,
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val titleLanguageSection = SortFilterSectionState.Dropdown(
        labelText = Res.string.anime_media_recommendations_filter_setting_title_language,
        values = AniListLanguageOption.entries,
        valueToText = { stringResource(it.textRes) },
        property = mediaDataSettings.languageOptionMedia,
    )

    private val sections = listOf(
        sortSection,
        titleLanguageSection,
        makeAdvancedSection(),
    )

    private val filterParams =
        combineStates(sortOption, sortAscending) { sortOption, sortAscending ->
            FilterParams(sort = sortOption, sortAscending = sortAscending)
        }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )

    data class FilterParams(
        val sort: RecommendationSortOption,
        val sortAscending: Boolean,
    )

    @AssistedFactory
    interface Factory {
        fun create(savedStateHandle: SavedStateHandle): MediaRecommendationsSortFilterViewModel
    }
}
