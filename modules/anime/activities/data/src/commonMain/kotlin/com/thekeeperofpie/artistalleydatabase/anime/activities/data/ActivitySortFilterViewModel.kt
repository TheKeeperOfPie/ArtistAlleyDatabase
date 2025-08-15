package com.thekeeperofpie.artistalleydatabase.anime.activities.data

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.activities.data.generated.resources.Res
import artistalleydatabase.modules.anime.activities.data.generated.resources.anime_activity_filter_date
import artistalleydatabase.modules.anime.activities.data.generated.resources.anime_activity_filter_date_content_description
import artistalleydatabase.modules.anime.activities.data.generated.resources.anime_activity_filter_has_replies_label
import artistalleydatabase.modules.anime.activities.data.generated.resources.anime_activity_filter_media_expand_content_description
import artistalleydatabase.modules.anime.activities.data.generated.resources.anime_activity_filter_media_label
import artistalleydatabase.modules.anime.activities.data.generated.resources.anime_activity_filter_type_dropdown_content_description
import artistalleydatabase.modules.anime.activities.data.generated.resources.anime_activity_filter_type_include_exclude_icon_content_description
import artistalleydatabase.modules.anime.activities.data.generated.resources.anime_activity_filter_type_label
import artistalleydatabase.modules.anime.activities.data.generated.resources.anime_activity_sort_label
import com.anilist.data.fragment.MediaNavigationData
import com.anilist.data.type.ActivityType
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaNavigationDataImpl
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDetailsRoute
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.AiringDate
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.AiringDateAdvancedSection
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchSortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.CustomFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterExpandedState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@Inject
class ActivitySortFilterViewModel(
    aniListApi: AuthedAniListApi,
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaDataSettings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted mediaDetailsRoute: MediaDetailsRoute,
    @Assisted initialParams: InitialParams,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
) {
    private val sortOptionEnabled =
        savedStateHandle.getMutableStateFlow<ActivitySortOption>(
            json = json,
            key = "enabledSortOptions",
            initialValue = ActivitySortOption.PINNED,
        )
    private val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.anime_activity_sort_label,
        defaultSort = ActivitySortOption.PINNED,
        sortAscending = null,
        sortOption = sortOptionEnabled,
    )

    private val typeIn =
        savedStateHandle.getMutableStateFlow<Set<ActivityType>>(json, "typeIn", emptySet())
    private val typeNotIn =
        savedStateHandle.getMutableStateFlow<Set<ActivityType>>(json, "typeNotIn", emptySet())
    private val typeSection =
        if (initialParams.isMediaSpecific) null else SortFilterSectionState.Filter(
            title = Res.string.anime_activity_filter_type_label,
            titleDropdownContentDescription = Res.string.anime_activity_filter_type_dropdown_content_description,
            includeExcludeIconContentDescription = Res.string.anime_activity_filter_type_include_exclude_icon_content_description,
            options = MutableStateFlow(
                listOf(
                    ActivityType.TEXT,
                    ActivityType.ANIME_LIST,
                    ActivityType.MANGA_LIST,
                    ActivityType.MESSAGE,
                )
            ),
            filterIn = typeIn,
            filterNotIn = typeNotIn,
            valueToText = { stringResource(it.toTextRes()) },
        )

    private val hasReplies = savedStateHandle.getMutableStateFlow<Boolean>("hasReplies", false)
    private val hasRepliesSection = SortFilterSectionState.Switch(
        title = Res.string.anime_activity_filter_has_replies_label,
        defaultEnabled = false,
        enabled = hasReplies,
    )

    private val date = savedStateHandle.getMutableStateFlow(json, "date") { AiringDate.Advanced() }
    private val dateShown = savedStateHandle.getMutableStateFlow<Boolean?>(json, "dateShown", null)
    private val dateSection = object : SortFilterSectionState.Custom("date") {
        override fun clear() {
            date.value = AiringDate.Advanced()
            dateShown.value = null
        }

        @Composable
        override fun isDefault() = date.collectAsStateWithLifecycle().value.summaryText() == null

        @Composable
        override fun Content(state: SortFilterExpandedState, showDivider: Boolean) {
            var date by date.collectAsMutableStateWithLifecycle()
            var dateShown by dateShown.collectAsMutableStateWithLifecycle()
            val expanded = state.expandedState[id] == true
            CustomFilterSection(
                expanded = expanded,
                onExpandedChange = { state.expandedState[id] = it },
                titleRes = Res.string.anime_activity_filter_date,
                titleDropdownContentDescriptionRes = Res.string.anime_activity_filter_date_content_description,
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
            if (dateShown != null) {
                StartEndDateDialog(
                    shownForStartDate = dateShown,
                    onShownForStartDateChange = { dateShown = it },
                    onDateChange = ::onReleaseDateChange,
                )
            }
        }
    }

    val mediaSelected =
        savedStateHandle.getMutableStateFlow<MediaNavigationDataImpl?>(json, "mediaSelected", null)
    private val mediaQuery = savedStateHandle.getMutableStateFlow("mediaQuery", "")
    private val mediaSection =
        if (initialParams.isMediaSpecific) null else MediaSearchSortFilterSection(
            titleTextRes = Res.string.anime_activity_filter_media_label,
            titleDropdownContentDescriptionRes = Res.string.anime_activity_filter_media_expand_content_description,
            scope = viewModelScope,
            aniListApi = aniListApi,
            mediaDataSettings = mediaDataSettings,
            mediaType = null,
            mediaSharedElement = initialParams.mediaSharedElement,
            mediaDetailsRoute = mediaDetailsRoute,
            mediaSelected = mediaSelected,
            query = mediaQuery,
        )

    private fun onReleaseDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.UTC)
                .date
        }

        date.update {
            if (start) {
                it.copy(startDate = selectedDate)
            } else {
                it.copy(endDate = selectedDate)
            }
        }
    }

    private val sections = listOfNotNull(
        sortSection,
        typeSection,
        hasRepliesSection,
        dateSection,
        mediaSection,
        makeAdvancedSection(),
    )

    @Suppress("UNCHECKED_CAST")
    private val filterParams = combineStates(
        sortOptionEnabled,
        typeIn,
        typeNotIn,
        hasReplies,
        date,
        mediaSelected,
    ) {
        FilterParams(
            sort = it[0] as ActivitySortOption,
            typeIn = it[1] as Set<ActivityType>,
            typeNotIn = it[2] as Set<ActivityType>,
            hasReplies = it[3] as Boolean,
            date = it[4] as AiringDate.Advanced,
            mediaId = (it[5] as MediaNavigationData?)?.id?.toString(),
        )
    }.debounceState(viewModelScope, 1.seconds)

    val state = SortFilterState<FilterParams>(
        sections = sections,
        filterParams = filterParams,
        collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose,
    )

    data class InitialParams(
        val mediaSharedElement: Boolean,
        val isMediaSpecific: Boolean,
    )

    data class FilterParams(
        val sort: ActivitySortOption,
        val typeIn: Set<ActivityType>,
        val typeNotIn: Set<ActivityType>,
        val hasReplies: Boolean,
        val date: AiringDate.Advanced,
        val mediaId: String?,
    ) {
        /** To avoid fetching the first page of media activity if it matches the vanilla request */
        val isDefault = sort == ActivitySortOption.PINNED
                && typeIn.isEmpty()
                && typeNotIn.isEmpty()
                && !hasReplies
                && date.startDate == null
                && date.endDate == null
                && mediaId == null
    }
}
