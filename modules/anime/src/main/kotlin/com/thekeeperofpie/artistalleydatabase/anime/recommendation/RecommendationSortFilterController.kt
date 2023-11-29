package com.thekeeperofpie.artistalleydatabase.anime.recommendation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.MediaSearchSortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class RecommendationSortFilterController(
    screenKey: String,
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : SortFilterController<RecommendationSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = RecommendationSortOption::class,
        defaultEnabled = RecommendationSortOption.ID,
        headerTextRes = R.string.anime_recommendation_filter_sort_label,
    )

    private val onListSection = SortFilterSection.Switch(
        titleRes = R.string.anime_recommendation_filter_on_list_label,
        defaultEnabled = true,
    )

    private val sourceMediaSection = MediaSearchSortFilterSection(
        screenKey = screenKey,
        id = "sourceMedia",
        titleTextRes = R.string.anime_recommendation_filter_source_media_label,
        titleDropdownContentDescriptionRes = R.string.anime_recommendation_filter_source_media_expand_content_description,
        scope = scope,
        aniListApi = aniListApi,
        settings = settings,
        mediaType = null,
    )

    private val targetMediaSection = MediaSearchSortFilterSection(
        screenKey = screenKey,
        id = "targetMedia",
        titleTextRes = R.string.anime_recommendation_filter_target_media_label,
        titleDropdownContentDescriptionRes = R.string.anime_recommendation_filter_target_media_expand_content_description,
        scope = scope,
        aniListApi = aniListApi,
        settings = settings,
        mediaType = null,
    )

    private val ratingSection = SortFilterSection.Range(
        titleRes = R.string.anime_recommendation_filter_rating_label,
        titleDropdownContentDescriptionRes = R.string.anime_recommendation_filter_rating_expand_content_description,
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
