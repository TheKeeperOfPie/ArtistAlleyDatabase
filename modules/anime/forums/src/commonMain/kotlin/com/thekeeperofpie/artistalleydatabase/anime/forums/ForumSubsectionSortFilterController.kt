package com.thekeeperofpie.artistalleydatabase.anime.forums

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import artistalleydatabase.modules.anime.forums.generated.resources.Res
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_category_chip_state_content_description
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_category_dropdown_content_description
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_category_label
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_media_expand_content_description
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_media_label
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_filter_subscribed_label
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_sort_label
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchSortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import org.jetbrains.compose.resources.stringResource

@OptIn(FlowPreview::class)
class ForumSubsectionSortFilterController(
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    settings: MediaDataSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    mediaDetailsRoute: MediaDetailsRoute,
) : MediaDataSettingsSortFilterController<ForumSubsectionSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
) {

    private val sortSection = SortFilterSection.Sort(
        ForumThreadSortOption::class,
        defaultEnabled = ForumThreadSortOption.SEARCH_MATCH,
        headerTextRes = Res.string.anime_forum_sort_label,
    )

    private val subscribedSection = SortFilterSection.Switch(
        titleRes = Res.string.anime_forum_filter_subscribed_label,
        defaultEnabled = false,
    )

    private val categorySection = SortFilterSection.Filter(
        titleRes = Res.string.anime_forum_filter_category_label,
        titleDropdownContentDescriptionRes = Res.string.anime_forum_filter_category_dropdown_content_description,
        includeExcludeIconContentDescriptionRes = Res.string.anime_forum_filter_category_chip_state_content_description,
        values = ForumCategoryOption.values().toList(),
        valueToText = { stringResource(it.value.textRes) },
        selectionMethod = SortFilterSection.Filter.SelectionMethod.SINGLE_EXCLUSIVE,
    )

    private val mediaSection = MediaSearchSortFilterSection(
        titleTextRes = Res.string.anime_forum_filter_media_label,
        titleDropdownContentDescriptionRes = Res.string.anime_forum_filter_media_expand_content_description,
        scope = scope,
        aniListApi = aniListApi,
        settings = settings,
        mediaType = null,
        mediaDetailsRoute = mediaDetailsRoute,
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
