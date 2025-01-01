package com.thekeeperofpie.artistalleydatabase.anime.forums

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.forums.generated.resources.Res
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_category_chip_state_content_description
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_category_dropdown_content_description
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_category_label
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_media_expand_content_description
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_media_label
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_subscribed_label
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_sort_label
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaNavigationDataImpl
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchSortFilterSection2
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
class ForumSubsectionSortFilterViewModel(
    aniListApi: AuthedAniListApi,
    json: Json,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaDetailsRoute: MediaDetailsRoute,
    @Assisted initialParams: InitialParams,
) : ViewModel() {
    private val sortOption = savedStateHandle.getMutableStateFlow(
        "sortOption",
        initialParams.defaultSort ?: ForumThreadSortOption.SEARCH_MATCH,
    )
    private val sortAscending =
        savedStateHandle.getMutableStateFlow<Boolean>("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        enumClass = ForumThreadSortOption::class,
        headerText = Res.string.anime_forum_sort_label,
        defaultSort = initialParams.defaultSort ?: ForumThreadSortOption.SEARCH_MATCH,
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val subscribed = savedStateHandle.getMutableStateFlow("subscribed", false)
    private val subscribedSection = SortFilterSectionState.Switch(
        title = Res.string.anime_forum_filter_subscribed_label,
        defaultEnabled = false,
        enabled = subscribed,
    )

    private val categoryIn = savedStateHandle
        .getMutableStateFlow<Set<ForumCategoryOption>>(json, "categoryIn", emptySet())
    private val categorySection = if (initialParams.categoryId != null) null else SortFilterSectionState.Filter(
        title = Res.string.anime_forum_filter_category_label,
        titleDropdownContentDescription = Res.string.anime_forum_filter_category_dropdown_content_description,
        includeExcludeIconContentDescription = Res.string.anime_forum_filter_category_chip_state_content_description,
        options = MutableStateFlow(ForumCategoryOption.entries),
        filterIn = categoryIn,
        filterNotIn = MutableStateFlow(emptySet()),
        valueToText = { stringResource(it.textRes) },
        selectionMethod = SortFilterSectionState.Filter.SelectionMethod.AT_MOST_ONE,
    )

    private val mediaSelected =
        savedStateHandle.getMutableStateFlow<MediaNavigationDataImpl?>(json, "mediaSelected", null)
    private val mediaQuery = savedStateHandle.getMutableStateFlow("mediaQuery", "")
    private val mediaSection =
        if (initialParams.mediaCategoryId != null) null else MediaSearchSortFilterSection2(
            titleTextRes = Res.string.anime_forum_filter_media_label,
            titleDropdownContentDescriptionRes = Res.string.anime_forum_filter_media_expand_content_description,
            scope = viewModelScope,
            aniListApi = aniListApi,
            mediaDataSettings = mediaDataSettings,
            mediaType = null,
            mediaSharedElement = true,
            mediaDetailsRoute = mediaDetailsRoute,
            mediaSelected = mediaSelected,
            query = mediaQuery,
        )

    private val sections =
        listOfNotNull(sortSection, subscribedSection, categorySection, mediaSection)

    @Suppress("UNCHECKED_CAST")
    private val filterParams =
        combineStates(sortOption, sortAscending, subscribed, categoryIn, mediaSelected) {
            FilterParams(
                sort = it[0] as ForumThreadSortOption,
                sortAscending = it[1] as Boolean,
                subscribed = it[2] as Boolean,
                categoryId = ((it[3] as Set<ForumCategoryOption>).singleOrNull()
                    ?: initialParams.categoryId?.let { categoryId ->
                        ForumCategoryOption.entries.find { it.categoryId.toString() == categoryId }
                    })?.categoryId?.toString(),
                mediaCategoryId = ((it[4]) as MediaNavigationData?)?.id?.toString()
                    ?: initialParams.mediaCategoryId,
            )
        }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )

    data class InitialParams(
        val defaultSort: ForumThreadSortOption?,
        val categoryId: String? = null,
        val mediaCategoryId: String? = null,
    )

    data class FilterParams(
        val sort: ForumThreadSortOption,
        val sortAscending: Boolean,
        val subscribed: Boolean,
        val categoryId: String?,
        val mediaCategoryId: String?,
    )
}
