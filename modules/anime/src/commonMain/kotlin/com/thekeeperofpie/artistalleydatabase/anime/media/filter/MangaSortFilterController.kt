package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_chapters_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_chapters_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_format_chip_state_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_format_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_format_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_release_date
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_release_date_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_volumes_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_volumes_label
import com.anilist.data.type.MediaFormat
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.AiringDate
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.AiringDateAdvancedSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.CustomFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
open class MangaSortFilterController<SortType : SortOption>(
    sortTypeEnumClass: KClass<SortType>,
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    mediaTagsController: MediaTagsController,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
) : MediaSortFilterController<SortType, MangaSortFilterController.InitialParams<SortType>>(
    sortTypeEnumClass = sortTypeEnumClass,
    scope = scope,
    aniListApi = aniListApi,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
    mediaTagsController = mediaTagsController,
    mediaGenresController = mediaGenresController,
    mediaLicensorsController = mediaLicensorsController,
    mediaType = MediaType.MANGA,
) {
    protected val formatSection = SortFilterSection.Filter(
        titleRes = Res.string.anime_media_filter_format_label,
        titleDropdownContentDescriptionRes = Res.string.anime_media_filter_format_content_description,
        includeExcludeIconContentDescriptionRes = Res.string.anime_media_filter_format_chip_state_content_description,
        values = listOf(
            MediaFormat.MANGA,
            MediaFormat.NOVEL,
            MediaFormat.ONE_SHOT,
        ),
        valueToText = { stringResource(it.value.toTextRes()) },
    )

    private var releaseDate by mutableStateOf(AiringDate.Advanced())
    private var releaseDateShown by mutableStateOf<Boolean?>(null)

    protected val releaseDateSection = object : SortFilterSection.Custom("releaseDate") {

        override fun showingPreview() = releaseDate.summaryText() != null

        override fun clear() {
            releaseDate = AiringDate.Advanced()
            releaseDateShown = null
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            val expanded = state.expandedState[id] ?: false
            CustomFilterSection(
                expanded = expanded,
                onExpandedChange = { state.expandedState[id] = it },
                titleRes = Res.string.anime_media_filter_release_date,
                titleDropdownContentDescriptionRes = Res.string.anime_media_filter_release_date_content_description,
                summaryText = { releaseDate.summaryText() },
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

    // TODO: Fix volumes/chapters range search
    protected val volumesSection = SortFilterSection.Range(
        titleRes = Res.string.anime_media_filter_volumes_label,
        titleDropdownContentDescriptionRes = Res.string.anime_media_filter_volumes_expand_content_description,
        initialData = RangeData(151),
        unboundedMax = true,
    )
    protected val chaptersSection = SortFilterSection.Range(
        titleRes = Res.string.anime_media_filter_chapters_label,
        titleDropdownContentDescriptionRes = Res.string.anime_media_filter_chapters_expand_content_description,
        initialData = RangeData(151),
        unboundedMax = true,
    )

    override var sections by mutableStateOf(emptyList<SortFilterSection>())

    open fun initialize(initialParams: InitialParams<SortType>) {
        super.initialize(initialParams)
        scope.launch(CustomDispatchers.Main) {
            aniListApi.authedUser
                .mapLatest { viewer ->
                    listOfNotNull(
                        sortSection.apply {
                            if (initialParams.defaultSort != null) {
                                changeDefault(
                                    initialParams.defaultSort,
                                    sortAscending = false,
                                    lockSort = initialParams.lockSort,
                                )
                            }
                        },
                        statusSection,
                        formatSection,
                        genreSection,
                        tagSection,
                        releaseDateSection.takeIf { initialParams.airingDateEnabled },
                        myListStatusSection.takeIf { viewer != null }?.apply {
                            if (filterOptions.none { it.value == null }) {
                                filterOptions =
                                    filterOptions + FilterEntry.FilterEntryImpl(null)
                            }

                            if (initialParams.mediaListStatus != null) {
                                setIncluded(
                                    initialParams.mediaListStatus,
                                    initialParams.lockMediaListStatus,
                                )
                            }
                        },
                        volumesSection,
                        chaptersSection,
                        sourceSection,
                        licensedBySection,
                        titleLanguageSection,
                        advancedSection.apply {
                            children = listOfNotNull(
                                showAdultSection,
                                collapseOnCloseSection,
                                hideIgnoredSection.takeIf { initialParams.hideIgnoredEnabled },
                                showLessImportantTagsSection,
                                showSpoilerTagsSection,
                            )
                        },
                        SortFilterSection.Spacer(height = 32.dp),
                    )
                }
                .collectLatest { sections = it }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Composable
    override fun filterParams() = FilterParams(
        sort = sortSection.sortOptions,
        sortAscending = sortSection.sortAscending,
        genres = genreSection.filterOptions,
        tagsByCategory = tagsByCategoryFiltered.collectAsState(emptyMap()).value,
        tagRank = tagRank.toIntOrNull()?.coerceIn(0, 100),
        statuses = statusSection.filterOptions,
        myListStatuses = myListStatusSection.filterOptions.filter { it.value != null }
                as List<FilterEntry<MediaListStatus>>,
        onList = when (myListStatusSection.filterOptions.find { it.value == null }?.state) {
            FilterIncludeExcludeState.INCLUDE -> true
            FilterIncludeExcludeState.EXCLUDE -> false
            FilterIncludeExcludeState.DEFAULT,
            null,
            -> null
        },
        formats = formatSection.filterOptions,
        averageScoreRange = averageScoreSection.data,
        episodesRange = null,
        volumesRange = volumesSection.data,
        chaptersRange = chaptersSection.data,
        showAdult = settings.showAdult.collectAsState().value,
        showIgnored = settings.showIgnored.collectAsState(false).value,
        airingDate = initialParams?.year
            ?.let { AiringDate.Basic(seasonYear = it.toString()) }
            ?: releaseDate,
        sources = sourceSection.filterOptions,
        licensedBy = licensedBySection.children.flatMap { it.filterOptions },
        theirListStatuses = null,
        myScore = null,
        theirScore = null,
    )

    fun onReleaseDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.UTC)
                .date
        }

        releaseDate = if (start) {
            releaseDate.copy(startDate = selectedDate)
        } else {
            releaseDate.copy(endDate = selectedDate)
        }
    }

    @Composable
    override fun PromptDialog() {
        if (releaseDateShown != null) {
            StartEndDateDialog(
                shownForStartDate = releaseDateShown,
                onShownForStartDateChange = { releaseDateShown = it },
                onDateChange = ::onReleaseDateChange,
            )
        }
    }

    data class InitialParams<SortType : SortOption>(
        override val tagId: String? = null,
        override val genre: String? = null,
        override val year: Int? = null,
        val airingDateEnabled: Boolean = year == null,
        val hideIgnoredEnabled: Boolean = true,
        val defaultSort: SortType?,
        val lockSort: Boolean,
        val mediaListStatus: MediaListStatus? = null,
        val lockMediaListStatus: Boolean = false,
    ) : MediaSortFilterController.InitialParams<SortType>
}
