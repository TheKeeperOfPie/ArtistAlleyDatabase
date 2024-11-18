package com.thekeeperofpie.artistalleydatabase.anime.recommendations

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.recommendations.generated.resources.Res
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_on_list_label
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_rating_expand_content_description
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_rating_label
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_sort_label
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_source_media_expand_content_description
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_source_media_label
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_target_media_expand_content_description
import artistalleydatabase.modules.anime.recommendations.generated.resources.anime_recommendation_filter_target_media_label
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchSortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope

class RecommendationSortFilterController(
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    settings: MediaDataSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val mediaDetailsRoute: MediaDetailsRoute,
) : MediaDataSettingsSortFilterController<RecommendationSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = RecommendationSortOption::class,
        defaultEnabled = RecommendationSortOption.ID,
        headerTextRes = Res.string.anime_recommendation_filter_sort_label,
    )

    private val onListSection = SortFilterSection.Switch(
        titleRes = Res.string.anime_recommendation_filter_on_list_label,
        defaultEnabled = true,
    )

    private val sourceMediaSection = MediaSearchSortFilterSection(
        id = "sourceMedia",
        titleTextRes = Res.string.anime_recommendation_filter_source_media_label,
        titleDropdownContentDescriptionRes = Res.string.anime_recommendation_filter_source_media_expand_content_description,
        scope = scope,
        aniListApi = aniListApi,
        settings = settings,
        mediaType = null,
        mediaDetailsRoute = mediaDetailsRoute,
    )

    private val targetMediaSection = MediaSearchSortFilterSection(
        id = "targetMedia",
        titleTextRes = Res.string.anime_recommendation_filter_target_media_label,
        titleDropdownContentDescriptionRes = Res.string.anime_recommendation_filter_target_media_expand_content_description,
        scope = scope,
        aniListApi = aniListApi,
        settings = settings,
        mediaType = null,
        mediaDetailsRoute = mediaDetailsRoute,
    )

    private val ratingSection = SortFilterSection.Range(
        titleRes = Res.string.anime_recommendation_filter_rating_label,
        titleDropdownContentDescriptionRes = Res.string.anime_recommendation_filter_rating_expand_content_description,
        initialData = RangeData(200),
    )

    override var sections = listOf(
        sortSection,
        onListSection,
        sourceMediaSection,
        targetMediaSection,
        ratingSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
        sourceMediaId = sourceMediaSection.selectedMedia?.id?.toString(),
        targetMediaId = targetMediaSection.selectedMedia?.id?.toString(),
        ratingRange = ratingSection.data,
        onList = onListSection.enabled,
    )

    data class FilterParams(
        val sort: List<SortEntry<RecommendationSortOption>>,
        val sortAscending: Boolean,
        val sourceMediaId: String?,
        val targetMediaId: String?,
        val ratingRange: RangeData,
        val onList: Boolean,
    )
}
