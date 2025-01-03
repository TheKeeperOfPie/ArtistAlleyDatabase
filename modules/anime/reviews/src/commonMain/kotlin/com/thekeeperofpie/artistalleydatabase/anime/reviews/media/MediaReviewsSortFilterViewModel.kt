package com.thekeeperofpie.artistalleydatabase.anime.reviews.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.reviews.generated.resources.Res
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_media_reviews_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.reviews.ReviewSortOption
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.time.Duration.Companion.seconds

@Inject
class MediaReviewsSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    // TODO: Can SavedStateHandle hold enums without Json?
    private val sortOption =
        savedStateHandle.getMutableStateFlow(json, "sortOption", ReviewSortOption.RATING)
    private val sortAscending = savedStateHandle.getMutableStateFlow("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        defaultSort = ReviewSortOption.RATING,
        headerText = Res.string.anime_media_reviews_filter_sort_label,
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val sections = listOf(sortSection)

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
        val sort: ReviewSortOption,
        val sortAscending: Boolean,
    )
}
