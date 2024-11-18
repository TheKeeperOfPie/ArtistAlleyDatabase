package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_user_filter_moderator_label
import artistalleydatabase.modules.anime.generated.resources.anime_user_filter_sort_label
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope

class UserSortFilterController(
    scope: CoroutineScope,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : MediaDataSettingsSortFilterController<UserSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = UserSortOption::class,
        defaultEnabled = UserSortOption.SEARCH_MATCH,
        headerTextRes = Res.string.anime_user_filter_sort_label,
    )

    private val moderatorSection = SortFilterSection.Switch(
        titleRes = Res.string.anime_user_filter_moderator_label,
        defaultEnabled = false,
    )

    override var sections = listOf(
        sortSection,
        moderatorSection,
        advancedSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
        isModerator = moderatorSection.enabled,
    )

    data class FilterParams(
        val sort: List<SortEntry<UserSortOption>>,
        val sortAscending: Boolean,
        val isModerator: Boolean,
    )
}
