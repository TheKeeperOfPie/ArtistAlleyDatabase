package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.recommendations.generated.resources.Res
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_on_list_label
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_rating_expand_content_description
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_rating_label
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_sort_label
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_source_media_expand_content_description
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_source_media_label
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_target_media_expand_content_description
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_target_media_label
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaNavigationDataImpl
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchSortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class RecommendationsSortFilterViewModel(
    aniListApi: AuthedAniListApi,
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaDetailsRoute: MediaDetailsRoute,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    private val sortOption =
        savedStateHandle.getMutableStateFlow(json, "sortOption", RecommendationSortOption.ID)
    private val sortAscending = savedStateHandle.getMutableStateFlow("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.anime_recommendation_filter_sort_label,
        defaultSort = RecommendationSortOption.ID,
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val onList = savedStateHandle.getMutableStateFlow("onList", true)
    private val onListSection = SortFilterSectionState.Switch(
        title = Res.string.anime_recommendation_filter_on_list_label,
        defaultEnabled = true,
        enabled = onList,
    )

    private val sourceMedia =
        savedStateHandle.getMutableStateFlow<MediaNavigationDataImpl?>(json, "sourceMedia", null)
    private val sourceMediaQuery = savedStateHandle.getMutableStateFlow("sourceMediaQuery", "")
    private val sourceMediaSection = MediaSearchSortFilterSection(
        id = "sourceMedia",
        titleTextRes = Res.string.anime_recommendation_filter_source_media_label,
        titleDropdownContentDescriptionRes = Res.string.anime_recommendation_filter_source_media_expand_content_description,
        scope = viewModelScope,
        aniListApi = aniListApi,
        mediaDataSettings = mediaDataSettings,
        mediaType = null,
        mediaDetailsRoute = mediaDetailsRoute,
        mediaSelected = sourceMedia,
        query = sourceMediaQuery,
    )

    private val targetMedia =
        savedStateHandle.getMutableStateFlow<MediaNavigationDataImpl?>(json, "targetMedia", null)
    private val targetMediaQuery = savedStateHandle.getMutableStateFlow("targetMediaQuery", "")
    private val targetMediaSection = MediaSearchSortFilterSection(
        id = "targetMedia",
        titleTextRes = Res.string.anime_recommendation_filter_target_media_label,
        titleDropdownContentDescriptionRes = Res.string.anime_recommendation_filter_target_media_expand_content_description,
        scope = viewModelScope,
        aniListApi = aniListApi,
        mediaDataSettings = mediaDataSettings,
        mediaType = null,
        mediaDetailsRoute = mediaDetailsRoute,
        mediaSelected = targetMedia,
        query = targetMediaQuery,
    )

    private val rating = savedStateHandle.getMutableStateFlow(json, "rating", RangeData(200))
    private val ratingSection = SortFilterSectionState.Range(
        title = Res.string.anime_recommendation_filter_rating_label,
        titleDropdownContentDescription = Res.string.anime_recommendation_filter_rating_expand_content_description,
        initialData = RangeData(200),
        data = rating,
    )

    private val sections = listOf(
        sortSection,
        onListSection,
        sourceMediaSection,
        targetMediaSection,
        ratingSection,
        makeAdvancedSection(),
    )

    private val filterParams = combineStates(sortOption, sortAscending, sourceMedia, targetMedia, rating, onList) {
        FilterParams(
            sort = it[0] as RecommendationSortOption,
            sortAscending = it[1] as Boolean,
            sourceMediaId = (it[2] as MediaNavigationDataImpl?)?.id?.toString(),
            targetMediaId = (it[3] as MediaNavigationDataImpl?)?.id?.toString(),
            ratingRange = it[4] as RangeData,
            onList = it[5] as Boolean,
        )
    }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )

    data class FilterParams(
        val sort: RecommendationSortOption,
        val sortAscending: Boolean,
        val sourceMediaId: String?,
        val targetMediaId: String?,
        val ratingRange: RangeData,
        val onList: Boolean,
    )

    @AssistedFactory
    interface Factory {
        fun create(
            savedStateHandle: SavedStateHandle,
            mediaDetailsRoute: MediaDetailsRoute,
        ): RecommendationsSortFilterViewModel
    }
}
