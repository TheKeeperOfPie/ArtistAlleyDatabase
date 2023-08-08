package com.thekeeperofpie.artistalleydatabase.anime.forum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class ForumSubsectionSortFilterController(
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
) : SortFilterController(settings, featureOverrideProvider) {

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

    override var sections by mutableStateOf(emptyList<SortFilterSection>())

    private lateinit var initialParams: InitialParams

    fun initialize(initialParams: InitialParams) {
        this.initialParams = initialParams
        sections = listOfNotNull(
            sortSection.apply {
                if (initialParams.defaultSort != null) {
                    changeSelected(
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
                        changeSelected(category, locked = initialParams.lockCategory)
                    }
                }
            },
        )
    }

    fun filterParams() = snapshotFlow {
        FilterParams(
            sortOptions = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
            subscribed = subscribedSection.enabled,
            categories = categorySection.filterOptions,
            // TODO: Real media filtering
            mediaCategoryId = initialParams.mediaCategoryId,
        )
    }.flowOn(CustomDispatchers.Main)
        .debounce(500.milliseconds)

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
