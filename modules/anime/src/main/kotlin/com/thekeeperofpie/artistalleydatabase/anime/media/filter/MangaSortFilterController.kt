package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.compose.filter.CustomFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MangaSortFilterController<SortType : SortOption>(
    sortTypeEnumClass: KClass<SortType>,
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    mediaTagsController: MediaTagsController,
) : MediaSortFilterController<SortType, MangaSortFilterController.InitialParams<SortType>>(
    sortTypeEnumClass = sortTypeEnumClass,
    aniListApi = aniListApi,
    settings = settings,
    mediaTagsController = mediaTagsController,
    mediaType = MediaType.MANGA,
) {
    private val formatSection = SortFilterSection.Filter(
        titleRes = R.string.anime_media_filter_format_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_format_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_format_chip_state_content_description,
        values = listOf(
            MediaFormat.MANGA,
            MediaFormat.NOVEL,
            MediaFormat.ONE_SHOT,
        ),
        valueToText = { stringResource(it.value.toTextRes()) },
    )

    var releaseDate by mutableStateOf(AiringDate.Advanced())
    var releaseDateShown by mutableStateOf<Boolean?>(null)

    private val releaseDateSection = object : SortFilterSection.Custom("releaseDate") {
        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            val expanded = state.expandedState[id] ?: false
            CustomFilterSection(
                expanded = expanded,
                onExpandedChange = { state.expandedState[id] = it },
                titleRes = R.string.anime_media_filter_release_date,
                titleDropdownContentDescriptionRes = R.string.anime_media_filter_release_date_content_description,
                summaryText = {
                    val startDate =
                        releaseDate.startDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                    val endDate =
                        releaseDate.endDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

                    when {
                        startDate != null && endDate != null -> {
                            if (releaseDate.startDate == releaseDate.endDate) {
                                startDate
                            } else {
                                "$startDate - $endDate"
                            }
                        }
                        startDate != null -> "≥ $startDate"
                        endDate != null -> "≤ $endDate"
                        else -> null
                    }
                },
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
                        data = releaseDate,
                        onRequestDatePicker = { releaseDateShown = it },
                        onDateChange = ::onReleaseDateChange,
                    )
                }
            }
        }
    }

    private val episodesSection = SortFilterSection.Range(
        titleRes = R.string.anime_media_filter_episodes_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_episodes_expand_content_description,
        data = RangeData(151),
        unboundedMax = true,
    )

    private val actionsSection = object : SortFilterSection.Custom("actions") {
        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            // TODO("Not yet implemented")
        }
    }

    override var sections by mutableStateOf(emptyList<SortFilterSection>())

    fun initialize(
        viewModel: ViewModel,
        refreshUptimeMillis: MutableStateFlow<*>,
        initialParams: InitialParams<SortType>,
        tagLongClickListener: (String) -> Unit = { /* TODO */ },
    ) {
        super.initialize(
            viewModel,
            refreshUptimeMillis,
            initialParams,
            tagLongClickListener,
        )
        viewModel.viewModelScope.launch(CustomDispatchers.Main) {
            aniListApi.authedUser
                .mapLatest { viewer ->
                    listOfNotNull(
                        sortSection.apply { changeDefaultEnabled(initialParams.defaultSort) },
                        statusSection,
                        formatSection,
                        genreSection,
                        tagSection,
                        releaseDateSection.takeIf { initialParams.airingDateEnabled },
                        listStatusSection.takeIf { viewer != null }?.apply {
                            if (initialParams.onListEnabled) {
                                if (filterOptions.none { it.value == null }) {
                                    filterOptions =
                                        filterOptions + FilterEntry.FilterEntryImpl(null)
                                }
                            } else {
                                if (filterOptions.any { it.value == null }) {
                                    filterOptions = filterOptions.filter { it.value != null }
                                }
                            }
                        },
                        episodesSection,
                        sourceSection,
                        advancedSection.apply {
                            children = listOfNotNull(
                                showAdultSection,
                                collapseOnCloseSection,
                                showIgnoredSection.takeIf { initialParams.showIgnoredEnabled }
                            )
                        },
                        SortFilterSection.Spacer(height = 32.dp),
                        actionsSection,
                    )
                }
                .collectLatest { sections = it }
        }
    }

    fun filterParams() =
        combine(
            snapshotFlow {
                @Suppress("UNCHECKED_CAST")
                FilterParams(
                    sort = sortSection.sortOptions,
                    sortAscending = sortSection.sortAscending,
                    genres = genreSection.filterOptions,
                    tagsByCategory = emptyMap(),
                    tagRank = tagRank.toIntOrNull()?.coerceIn(0, 100),
                    statuses = statusSection.filterOptions,
                    listStatuses = listStatusSection.filterOptions.filter { it.value != null }
                            as List<FilterEntry<MediaListStatus>>,
                    onList = when (listStatusSection.filterOptions.find { it.value == null }?.state) {
                        FilterIncludeExcludeState.INCLUDE -> true
                        FilterIncludeExcludeState.EXCLUDE -> false
                        FilterIncludeExcludeState.DEFAULT,
                        null -> null
                    },
                    formats = formatSection.filterOptions,
                    averageScoreRange = averageScoreSection.data,
                    episodesRange = episodesSection.data,
                    showAdult = false,
                    showIgnored = true,
                    airingDate = releaseDate,
                    sources = sourceSection.filterOptions,
                )
            }.flowOn(CustomDispatchers.Main),
            settings.showAdult,
            settings.showIgnored,
            tagsByCategoryFiltered,
        ) { filterParams, showAdult, showIgnored, tagsByCategory ->
            filterParams.copy(
                tagsByCategory = tagsByCategory,
                showAdult = showAdult,
                showIgnored = showIgnored,
            )
        }.debounce(500.milliseconds)

    fun onReleaseDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
        }

        releaseDate = if (start) {
            releaseDate.copy(startDate = selectedDate)
        } else {
            releaseDate.copy(endDate = selectedDate)
        }
    }

    @Composable
    override fun collapseOnClose() = settings.collapseAnimeFiltersOnClose.collectAsState().value

    data class InitialParams<SortType : SortOption>(
        override val tagId: String? = null,
        override val tagsIncluded: Set<String> = emptySet(),
        override val tagsExcluded: Set<String> = emptySet(),
        override val genresIncluded: Set<String> = emptySet(),
        override val genresExcluded: Set<String> = emptySet(),
        val airingDateEnabled: Boolean = true,
        val onListEnabled: Boolean = true,
        val showIgnoredEnabled: Boolean = true,
        val defaultSort: SortType?,
    ) : MediaSortFilterController.InitialParams<SortType>
}
