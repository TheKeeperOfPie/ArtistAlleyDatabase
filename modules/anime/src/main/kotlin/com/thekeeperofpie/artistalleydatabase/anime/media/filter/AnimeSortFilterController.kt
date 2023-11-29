package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
open class AnimeSortFilterController<SortType : SortOption>(
    scope: CoroutineScope,
    sortTypeEnumClass: KClass<SortType>,
    aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    mediaTagsController: MediaTagsController,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    userScoreEnabled: Boolean,
) : MediaSortFilterController<SortType, AnimeSortFilterController.InitialParams<SortType>>(
    sortTypeEnumClass = sortTypeEnumClass,
    aniListApi = aniListApi,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider,
    mediaTagsController = mediaTagsController,
    mediaGenresController = mediaGenresController,
    mediaLicensorsController = mediaLicensorsController,
    mediaType = MediaType.ANIME,
    userScoreEnabled = userScoreEnabled,
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

    private val airingDateSection = object : SortFilterSection.Custom("airingDate") {
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
                    airingDate = airingDate.copy(first = airingDate.first.copy(seasonYear = it))
                },
                onIsAdvancedToggle = { airingDateIsAdvanced = it },
                onRequestDatePicker = { airingDateShown = it },
                onDateChange = ::onAiringDateChange,
                showDivider = showDivider,
            )
        }
    }

    private val episodesSection = SortFilterSection.Range(
        titleRes = R.string.anime_media_filter_episodes_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_episodes_expand_content_description,
        initialData = RangeData(151),
        unboundedMax = true,
    )

    override var sections by mutableStateOf(emptyList<SortFilterSection>())

    fun initialize(
        viewModel: ViewModel,
        refreshUptimeMillis: MutableStateFlow<*>,
        initialParams: InitialParams<SortType>,
    ) {
        super.initialize(
            viewModel,
            refreshUptimeMillis,
            initialParams,
        )
        viewModel.viewModelScope.launch(CustomDispatchers.Main) {
            aniListApi.authedUser
                .mapLatest { viewer ->
                    listOfNotNull(
                        sortSection.apply {
                            if (initialParams.defaultSort != null) {
                                changeSelected(
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

                            if (initialParams.mediaListStatus != null) {
                                setIncluded(
                                    initialParams.mediaListStatus,
                                    initialParams.lockMediaListStatus,
                                )
                            }
                        },
                        userScoreSection,
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
                .collectLatest { sections = it }
        }
    }

    override val filterParams = scope.launchMolecule(RecompositionMode.Immediate) {
        @Suppress("UNCHECKED_CAST")
        FilterParams(
            sort = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
            genres = genreSection.filterOptions,
            tagsByCategory = tagsByCategoryFiltered.collectAsState(emptyMap()).value,
            tagRank = tagRank.toIntOrNull()?.coerceIn(0, 100),
            statuses = statusSection.filterOptions,
            listStatuses = listStatusSection.filterOptions.filter { it.value != null }
                    as List<FilterEntry<MediaListStatus>>,
            onList = when (listStatusSection.filterOptions.find { it.value == null }?.state) {
                FilterIncludeExcludeState.INCLUDE -> true
                FilterIncludeExcludeState.EXCLUDE -> false
                FilterIncludeExcludeState.DEFAULT,
                null,
                -> null
            },
            userScore = userScoreSection?.data,
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
        )
    }.debounce(500.milliseconds)

    override val filterParamsStateFlow = scope.launchMolecule(RecompositionMode.Immediate) {
        @Suppress("UNCHECKED_CAST")
        val newState = FilterParams(
            sort = sortSection.sortOptions,
            sortAscending = sortSection.sortAscending,
            genres = genreSection.filterOptions,
            tagsByCategory = tagsByCategoryFiltered.collectAsState(emptyMap()).value,
            tagRank = tagRank.toIntOrNull()?.coerceIn(0, 100),
            statuses = statusSection.filterOptions,
            listStatuses = listStatusSection.filterOptions.filter { it.value != null }
                    as List<FilterEntry<MediaListStatus>>,
            onList = when (listStatusSection.filterOptions.find { it.value == null }?.state) {
                FilterIncludeExcludeState.INCLUDE -> true
                FilterIncludeExcludeState.EXCLUDE -> false
                FilterIncludeExcludeState.DEFAULT,
                null,
                -> null
            },
            userScore = userScoreSection?.data,
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
        )
        var actualState by remember { mutableStateOf(newState) }
        LaunchedEffect(newState) {
            delay(500.milliseconds)
            actualState = newState
        }
        actualState
    }

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
