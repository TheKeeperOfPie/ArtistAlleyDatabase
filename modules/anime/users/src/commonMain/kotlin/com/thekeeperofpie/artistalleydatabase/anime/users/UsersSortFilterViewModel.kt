package com.thekeeperofpie.artistalleydatabase.anime.users

import androidx.lifecycle.SavedStateHandle
import artistalleydatabase.modules.anime.users.generated.resources.Res
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_filter_moderator_label
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.users.data.filter.UserSortOption
import com.thekeeperofpie.artistalleydatabase.anime.users.data.filter.UsersSortFilterParams
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class UsersSortFilterViewModel(
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    private val sortOption =
        savedStateHandle.getMutableStateFlow(json, "sortOption", UserSortOption.SEARCH_MATCH)
    private val sortAscending = savedStateHandle.getMutableStateFlow("sortAscending", false)
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.anime_user_filter_sort_label,
        defaultSort = UserSortOption.SEARCH_MATCH,
        sortOption = sortOption,
        sortAscending = sortAscending,
    )

    private val moderator = savedStateHandle.getMutableStateFlow<Boolean?>(json, "moderator", null)
    private val moderatorSection = SortFilterSectionState.TriStateBoolean(
        title = Res.string.anime_user_filter_moderator_label,
        defaultEnabled = false,
        enabled = moderator,
    )

    private val sections = listOf(sortSection, moderatorSection)

    private val filterParams = combineStates(sortOption, sortAscending, moderator) {
        UsersSortFilterParams(
            sort = it[0] as UserSortOption,
            sortAscending = it[1] as Boolean,
            isModerator = it[2] as Boolean?,
        )
    }

    val state = SortFilterState(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )
}
