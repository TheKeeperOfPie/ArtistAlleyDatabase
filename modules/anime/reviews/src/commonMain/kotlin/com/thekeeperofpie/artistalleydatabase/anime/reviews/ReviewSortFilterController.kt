package com.thekeeperofpie.artistalleydatabase.anime.reviews

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.reviews.generated.resources.Res
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_review_filter_media_expand_content_description
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_review_filter_media_label
import artistalleydatabase.modules.anime.reviews.generated.resources.anime_review_filter_sort_label
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchSortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
class ReviewSortFilterController(
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    settings: MediaDataSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    mediaType: MediaType?,
    private val sortSection: SortFilterSection.Sort<ReviewSortOption> = sortSection(),
    mediaDetailsRoute: MediaDetailsRoute,
) : MediaDataSettingsSortFilterController<ReviewSortFilterController.FilterParams>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider
) {

    companion object {
        fun sortSection() = SortFilterSection.Sort(
            enumClass = ReviewSortOption::class,
            defaultEnabled = ReviewSortOption.CREATED_AT,
            headerTextRes = Res.string.anime_review_filter_sort_label,
        )
    }

    private val mediaSection = MediaSearchSortFilterSection(
        titleTextRes = Res.string.anime_review_filter_media_label,
        titleDropdownContentDescriptionRes = Res.string.anime_review_filter_media_expand_content_description,
        scope = scope,
        aniListApi = aniListApi,
        settings = settings,
        mediaType = mediaType,
        mediaDetailsRoute = mediaDetailsRoute,
    )

    override var sections = listOf(
        sortSection,
        mediaSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
        mediaId = mediaSection.selectedMedia?.id?.toString()
    )

    fun selectedMedia() = mediaSection.selectedMedia

    data class FilterParams(
        val sort: List<SortEntry<ReviewSortOption>>,
        val sortAscending: Boolean,
        val mediaId: String?,
    )
}
