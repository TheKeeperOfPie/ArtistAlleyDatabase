package com.thekeeperofpie.artistalleydatabase.anime.forum

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.AnimeSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.MediaSearchSortFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class ForumSubsectionSortFilterController(
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : AnimeSettingsSortFilterController<ForumSubsectionSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        ForumThreadSortOption::class,
        defaultEnabled = ForumThreadSortOption.SEARCH_MATCH,
        headerTextRes = R.string.anime_forum_sort_label,
    )

    private val subscribedSection = SortFilterSection.Switch(
        titleRes = R.string.anime_forum_filter_subscribed_label,
        defaultEnabled = false,
    )

    private val categorySection = SortFilterSection.Filter(
        titleRes = R.string.anime_forum_filter_category_label,
        titleDropdownContentDescriptionRes = R.string.anime_forum_filter_category_dropdown_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_forum_filter_category_chip_state_content_description,
        values = ForumCategoryOption.values().toList(),
        valueToText = { stringResource(it.value.textRes) },
        selectionMethod = SortFilterSection.Filter.SelectionMethod.SINGLE_EXCLUSIVE,
    )

    private val mediaSection = MediaSearchSortFilterSection(
        titleTextRes = R.string.anime_forum_filter_media_label,
        titleDropdownContentDescriptionRes = R.string.anime_forum_filter_media_expand_content_description,
        scope = scope,
        aniListApi = aniListApi,
        settings = settings,
        mediaType = null,
    )

    override var sections by mutableStateOf(emptyList<SortFilterSection>())

    private lateinit var initialParams: InitialParams

    fun initialize(initialParams: InitialParams) {
        this.initialParams = initialParams
        sections = listOfNotNull(
            sortSection.apply {
                if (initialParams.defaultSort != null) {
                    changeDefault(
                        initialParams.defaultSort,
                        sortAscending = false,
                        lockSort = initialParams.lockSort,
                    )
                }
            },
            subscribedSection,
            categorySection.apply {
                if (initialParams.categoryId != null) {
                    val category = ForumCategoryOption.values()
                        .find { it.categoryId == initialParams.categoryId.toIntOrNull() }
                    if (category != null) {
                        setIncluded(category, locked = initialParams.lockCategory)
                    }
                }
            },
            mediaSection.takeIf { initialParams.mediaCategoryId == null },
        )
    }

    @Composable
    override fun filterParams() = FilterParams(
        sortOptions = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
        subscribed = subscribedSection.enabled,
        categories = categorySection.filterOptions,
        mediaCategoryId = initialParams.mediaCategoryId
            ?: mediaSection.selectedMedia?.id?.toString(),
    )

    data class InitialParams(
        val defaultSort: ForumThreadSortOption? = null,
        val lockSort: Boolean = defaultSort != null,
        val categoryId: String? = null,
        val lockCategory: Boolean = categoryId != null,
        val mediaCategoryId: String? = null,
        val lockMediaCategory: Boolean = mediaCategoryId != null,
    )

    data class FilterParams(
        val sortOptions: List<SortEntry<ForumThreadSortOption>>,
        val sortAscending: Boolean,
        val subscribed: Boolean,
        val categories: List<FilterEntry<ForumCategoryOption>>,
        val mediaCategoryId: String?,
    )
}
