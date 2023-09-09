package com.thekeeperofpie.artistalleydatabase.anime.review

import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.MediaSearchSortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class ReviewSortFilterController(
    screenKey: String,
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    mediaType: MediaType?,
    private val sortSection: SortFilterSection.Sort<ReviewSortOption> = sortSection(),
) : SortFilterController<ReviewSortFilterController.FilterParams>(
    settings,
    featureOverrideProvider
) {

    companion object {
        fun sortSection() = SortFilterSection.Sort(
            enumClass = ReviewSortOption::class,
            defaultEnabled = ReviewSortOption.CREATED_AT,
            headerTextRes = R.string.anime_review_filter_sort_label,
        )
    }

    private val mediaSection = MediaSearchSortFilterSection(
        screenKey = screenKey,
        titleTextRes = R.string.anime_review_filter_media_label,
        titleDropdownContentDescriptionRes = R.string.anime_review_filter_media_expand_content_description,
        scope = scope,
        aniListApi = aniListApi,
        settings = settings,
        mediaType = mediaType,
    )

    override var sections = listOf(
        sortSection,
        mediaSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    override fun filterParams() = snapshotFlow {
        FilterParams(
            sort = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
            mediaId = mediaSection.selectedMedia?.id?.toString()
        )
    }.debounce(500.milliseconds)

    fun selectedMedia() = mediaSection.selectedMedia

    data class FilterParams(
        val sort: List<SortEntry<ReviewSortOption>>,
        val sortAscending: Boolean,
        val mediaId: String?,
    )
}
