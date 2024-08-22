package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
open class AnimeSortFilterController<SortType : SortOption>(
    sortTypeEnumClass: KClass<SortType>,
    scope: CoroutineScope,
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    mediaTagsController: MediaTagsController,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
) : MediaSortFilterController<SortType, AnimeSortFilterController.InitialParams<SortType>>(
    sortTypeEnumClass = sortTypeEnumClass,
    scope = scope,
    aniListApi = aniListApi,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
    mediaTagsController = mediaTagsController,
    mediaGenresController = mediaGenresController,
    mediaLicensorsController = mediaLicensorsController,
    mediaType = MediaType.ANIME,
) {
    protected val formatSection = SortFilterSection.Filter(
        titleRes = R.string.anime_media_filter_format_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_format_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_format_chip_state_content_description,
        values = listOf(
            MediaFormat.TV,
            MediaFormat.TV_SHORT,
            MediaFormat.MOVIE,
            MediaFormat.SPECIAL,
            MediaFormat.OVA,
            MediaFormat.ONA,
            MediaFormat.MUSIC,
            // MANGA, NOVEL, and ONE_SHOT excluded since not anime
        ),
        valueToText = { stringResource(it.value.toTextRes()) },
    )

    protected var airingDate by mutableStateOf(AiringDate.Basic() to AiringDate.Advanced())
    protected var airingDateIsAdvanced by mutableStateOf(false)
    private var airingDateShown by mutableStateOf<Boolean?>(null)

    protected val airingDateSection = object : SortFilterSection.Custom("airingDate") {
        override fun showingPreview() =
            when (val data = if (airingDateIsAdvanced) airingDate.second else airingDate.first) {
                is AiringDate.Advanced -> (data.startDate != null) || (data.endDate != null)
                is AiringDate.Basic -> (data.season != null)
                        || (data.seasonYear.toIntOrNull() != null)
            }

        override fun clear() {
            airingDate = AiringDate.Basic() to AiringDate.Advanced()
            airingDateIsAdvanced = false
            airingDateShown = null
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            AiringDateSection(
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                data = { if (airingDateIsAdvanced) airingDate.second else airingDate.first },
                onSeasonChange = {
                    val year = airingDate.first.seasonYear.ifEmpty {
                        when (it) {
                            AiringDate.SeasonOption.PREVIOUS ->
                                AniListUtils.getPreviousSeasonYear().second.toString()
                            AiringDate.SeasonOption.CURRENT ->
                                AniListUtils.getCurrentSeasonYear().second.toString()
                            AiringDate.SeasonOption.NEXT ->
                                AniListUtils.getNextSeasonYear().second.toString()
                            AiringDate.SeasonOption.WINTER,
                            AiringDate.SeasonOption.SPRING,
                            AiringDate.SeasonOption.SUMMER,
                            AiringDate.SeasonOption.FALL,
                            null,
                            -> ""
                        }
                    }
                    airingDate = airingDate.copy(
                        first = airingDate.first.copy(season = it, seasonYear = year)
                    )
                },
                onSeasonYearChange = {
                    airingDate = airingDate.copy(
                        first = airingDate.first.copy(
                            season = airingDate.first.season?.makeAbsolute(it.toIntOrNull()),
                            seasonYear = it
                        )
                    )
                },
                onIsAdvancedToggle = { airingDateIsAdvanced = it },
                onRequestDatePicker = { airingDateShown = it },
                onDateChange = ::onAiringDateChange,
                showDivider = showDivider,
            )
        }
    }

    protected val episodesSection = SortFilterSection.Range(
        titleRes = R.string.anime_media_filter_episodes_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_episodes_expand_content_description,
        initialData = RangeData(151),
        unboundedMax = true,
    )

    override val sections get() = internalSections
    override var internalSections by mutableStateOf(emptyList<SortFilterSection>())

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
                        airingDateSection.takeIf { initialParams.airingDateEnabled },
                        myListStatusSection.takeIf { viewer != null }?.apply {
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

                            if (initialParams.mediaListStatus != null) {
                                setIncluded(
                                    initialParams.mediaListStatus,
                                    initialParams.lockMediaListStatus,
                                )
                            }
                        },
                        episodesSection,
                        sourceSection,
                        licensedBySection,
                        titleLanguageSection,
                        suggestionsSection,
                        advancedSection.apply {
                            children = listOfNotNull(
                                showAdultSection,
                                collapseOnCloseSection,
                                hideIgnoredSection.takeIf { initialParams.showIgnoredEnabled },
                                showLessImportantTagsSection,
                                showSpoilerTagsSection,
                            )
                        },
                        SortFilterSection.Spacer(height = 32.dp),
                    )
                }
                .collectLatest { internalSections = it }
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
        episodesRange = episodesSection.data,
        volumesRange = null,
        chaptersRange = null,
        showAdult = settings.showAdult.collectAsState().value,
        showIgnored = settings.showIgnored.collectAsState(false).value,
        airingDate = initialParams?.year?.let { AiringDate.Basic(seasonYear = it.toString()) }
            ?: if (airingDateIsAdvanced) airingDate.second else airingDate.first,
        sources = sourceSection.filterOptions,
        licensedBy = licensedBySection.children.flatMap { it.filterOptions },
        // TODO: See if these can be pushed down
        theirListStatuses = null,
        myScore = null,
        theirScore = null,
    )

    fun onAiringDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
        }

        airingDate = airingDate.copy(
            second = if (start) {
                airingDate.second.copy(startDate = selectedDate)
            } else {
                airingDate.second.copy(endDate = selectedDate)
            }
        )
    }

    @Composable
    override fun PromptDialog() {
        if (airingDateShown != null) {
            StartEndDateDialog(
                shownForStartDate = airingDateShown,
                onShownForStartDateChange = { airingDateShown = it },
                onDateChange = ::onAiringDateChange,
            )
        }
    }

    data class InitialParams<SortType : SortOption>(
        override val tagId: String? = null,
        override val genre: String? = null,
        override val year: Int? = null,
        val airingDateEnabled: Boolean = year == null,
        val onListEnabled: Boolean = true,
        val showIgnoredEnabled: Boolean = true,
        val defaultSort: SortType?,
        val lockSort: Boolean,
        val mediaListStatus: MediaListStatus? = null,
        val lockMediaListStatus: Boolean = false,
    ) : MediaSortFilterController.InitialParams<SortType>
}
