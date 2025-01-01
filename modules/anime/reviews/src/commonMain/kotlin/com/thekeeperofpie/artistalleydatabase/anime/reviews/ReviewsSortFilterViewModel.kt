package com.thekeeperofpie.artistalleydatabase.anime.reviews

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.reviews.generated.resources.Res
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_review_filter_media_expand_content_description
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_review_filter_media_label
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_review_filter_sort_label
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaNavigationDataImpl
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchSortFilterSection
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
class ReviewsSortFilterViewModel(
    aniListApi: AuthedAniListApi,
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaDetailsRoute: MediaDetailsRoute,
    @Assisted mediaType: MediaType,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    private val sortOption =
        savedStateHandle.getMutableStateFlow(json, "sortOption", ReviewSortOption.CREATED_AT)
    private val sortAscending = savedStateHandle.getMutableStateFlow("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        enumClass = ReviewSortOption::class,
        headerText = Res.string.anime_review_filter_sort_label,
        defaultSort = ReviewSortOption.CREATED_AT,
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    val media =
        savedStateHandle.getMutableStateFlow<MediaNavigationDataImpl?>(json, "media", null)
    private val mediaQuery = savedStateHandle.getMutableStateFlow("mediaQuery", "")
    private val mediaSection = MediaSearchSortFilterSection(
        titleTextRes = Res.string.anime_review_filter_media_label,
        titleDropdownContentDescriptionRes = Res.string.anime_review_filter_media_expand_content_description,
        scope = viewModelScope,
        aniListApi = aniListApi,
        mediaDataSettings = mediaDataSettings,
        mediaType = mediaType,
        mediaDetailsRoute = mediaDetailsRoute,
        mediaSelected = media,
        query = mediaQuery,
    )

    private val sections = listOf(
        sortSection,
        mediaSection,
        makeAdvancedSection(), // TODO: Can this actually filter out ignored?
    )

    private val filterParams = combineStates(sortOption, sortAscending, media) {
        FilterParams(
            sort = it[0] as ReviewSortOption,
            sortAscending = it[1] as Boolean,
            mediaId = (it[2] as MediaNavigationDataImpl?)?.id?.toString(),
        )
    }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )

    data class FilterParams(
        val sort: ReviewSortOption,
        val sortAscending: Boolean,
        val mediaId: String?,
    )
}
