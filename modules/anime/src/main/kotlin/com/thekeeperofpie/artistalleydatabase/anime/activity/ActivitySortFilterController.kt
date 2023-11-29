package com.thekeeperofpie.artistalleydatabase.anime.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anilist.type.ActivityType
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ActivityUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.filter.MediaSearchSortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AiringDate
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.AiringDateAdvancedSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.compose.filter.CustomFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import java.time.Instant
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class ActivitySortFilterController(
    screenKey: String,
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    mediaSharedElement: Boolean = true,
) : SortFilterController<ActivitySortFilterController.FilterParams>(settings, featureOverrideProvider) {

    private val sortSection = SortFilterSection.Sort(
        enumClass = ActivitySortOption::class,
        defaultEnabled = ActivitySortOption.PINNED,
        headerTextRes = R.string.anime_activity_sort_label,
    )

    private val typeSection = SortFilterSection.Filter(
        titleRes = R.string.anime_activity_filter_type_label,
        titleDropdownContentDescriptionRes = R.string.anime_activity_filter_type_dropdown_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_activity_filter_type_include_exclude_icon_content_description,
        values = listOf(
            ActivityType.TEXT,
            ActivityType.ANIME_LIST,
            ActivityType.MANGA_LIST,
            ActivityType.MESSAGE,
        ),
        valueToText = { stringResource(it.value.toTextRes()) },
    )

    private val hasRepliesSection = SortFilterSection.Switch(
        titleRes = R.string.anime_activity_filter_has_replies_label,
        defaultEnabled = false,
    )

    var date by mutableStateOf(AiringDate.Advanced())
    var dateShown by mutableStateOf<Boolean?>(null)
    private val dateSection = object : SortFilterSection.Custom("date") {

        override fun showingPreview() = date.summaryText() != null

        override fun clear() {
            date = AiringDate.Advanced()
            dateShown = null
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            val expanded = state.expandedState[id] ?: false
            CustomFilterSection(
                expanded = expanded,
                onExpandedChange = { state.expandedState[id] = it },
                titleRes = R.string.anime_activity_filter_date,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_release_date_content_description,
                summaryText = { date.summaryText() },
                onSummaryClick = {
                    onReleaseDateChange(true, null)
                    onReleaseDateChange(false, null)
                },
                showDivider = showDivider,
            ) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    AiringDateAdvancedSection(
                        data = date,
                        onRequestDatePicker = { dateShown = it },
                        onDateChange = ::onReleaseDateChange,
                    )
                }
            }
        }
    }

    private val mediaSection = MediaSearchSortFilterSection(
        screenKey = screenKey,
        titleTextRes = R.string.anime_activity_filter_media_label,
        titleDropdownContentDescriptionRes = R.string.anime_activity_filter_media_expand_content_description,
        scope = scope,
        aniListApi = aniListApi,
        settings = settings,
        mediaType = null,
        mediaSharedElement = mediaSharedElement,
    )

    fun onReleaseDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
        }

        date = if (start) {
            date.copy(startDate = selectedDate)
        } else {
            date.copy(endDate = selectedDate)
        }
    }

    @Composable
    override fun PromptDialog() {
        if (dateShown != null) {
            StartEndDateDialog(
                shownForStartDate = dateShown,
                onShownForStartDateChange = { dateShown = it },
                onDateChange = ::onReleaseDateChange,
            )
        }
    }


    override val filterParams = snapshotFlow {
        FilterParams(
            sort = sortSection.sortOptions,
            type = typeSection.filterOptions,
            hasReplies = hasRepliesSection.enabled,
            date = date,
            mediaId = mediaSection.selectedMedia?.id?.toString(),
        )
    }.flowOn(CustomDispatchers.Main)
        .debounce(500.milliseconds)

    fun selectedMedia() = mediaSection.selectedMedia

    override val sections = listOf(
        sortSection,
        typeSection,
        hasRepliesSection,
        dateSection,
        mediaSection,
        SortFilterSection.Spacer(height = 32.dp),
    )

    data class FilterParams(
        val sort: List<SortEntry<ActivitySortOption>>,
        val type: List<FilterEntry.FilterEntryImpl<ActivityType>>,
        val hasReplies: Boolean,
        val date: AiringDate.Advanced,
        val mediaId: String?,
    )
}
