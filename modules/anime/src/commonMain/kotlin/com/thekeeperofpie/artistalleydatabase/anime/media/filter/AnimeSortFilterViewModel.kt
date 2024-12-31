package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_episodes_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_episodes_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_format_chip_state_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_format_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_format_label
import com.anilist.data.type.MediaFormat
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.AiringDate
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.AiringDateSection
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import com.thekeeperofpie.artistalleydatabase.utils_compose.ScopedSavedStateHandle
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

open class AnimeSortFilterViewModel<SortType>(
    aniListApi: AuthedAniListApi,
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    mediaTagsController: MediaTagsController,
    mediaDataSettings: MediaDataSettings,
    @Assisted sortOptions: MutableStateFlow<List<SortType>>? = null,
    @Assisted initialParams: InitialParams<SortType>,
    @Assisted savedStateHandle: ScopedSavedStateHandle,
) : MediaSortFilterViewModel<SortType>(
    aniListApi = aniListApi,
    featureOverrideProvider = featureOverrideProvider,
    json = json,
    mediaGenresController = mediaGenresController,
    mediaLicensorsController = mediaLicensorsController,
    mediaTagsController = mediaTagsController,
    mediaDataSettings = mediaDataSettings,
    sortOptions = sortOptions,
    initialParams = initialParams,
    savedStateHandle = savedStateHandle,
) where SortType : SortOption, SortType : Enum<SortType> {

    protected val formatIn = savedStateHandle.getMutableStateFlow<String, Set<MediaFormat>>(
        key = "formatIn",
        initialValue = { emptySet() },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )
    protected val formatNotIn = savedStateHandle.getMutableStateFlow<String, Set<MediaFormat>>(
        key = "formatNotIn",
        initialValue = { emptySet() },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )
    protected val formatSection = SortFilterSectionState.Filter(
        title = Res.string.anime_media_filter_format_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_format_content_description,
        includeExcludeIconContentDescription = Res.string.anime_media_filter_format_chip_state_content_description,
        options = MutableStateFlow(
            listOf(
                MediaFormat.TV,
                MediaFormat.TV_SHORT,
                MediaFormat.MOVIE,
                MediaFormat.SPECIAL,
                MediaFormat.OVA,
                MediaFormat.ONA,
                MediaFormat.MUSIC,
                // MANGA, NOVEL, and ONE_SHOT excluded since not anime
            )
        ),
        filterIn = formatIn,
        filterNotIn = formatNotIn,
        valueToText = { stringResource(it.toTextRes()) },
    )

    protected val airingDate =
        savedStateHandle.getMutableStateFlow<String, Pair<AiringDate.Basic, AiringDate.Advanced>>(
            key = "airingDate",
            initialValue = { AiringDate.Basic() to AiringDate.Advanced() },
            serialize = json::encodeToString,
            deserialize = json::decodeFromString,
        )
    protected val airingDateIsAdvanced =
        savedStateHandle.getMutableStateFlow<Boolean>("airingDateIsAdvanced") { false }
    private val airingDateShown = savedStateHandle.getMutableStateFlow<String, Boolean?>(
        key = "airingDateShown",
        initialValue = { null },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )

    protected val airingDateSection = object : SortFilterSectionState.Custom("airingDate") {
        override fun clear() {
            airingDate.value = AiringDate.Basic() to AiringDate.Advanced()
            airingDateIsAdvanced.value = false
            airingDateShown.value = null
        }

        @Composable
        override fun isDefault(): Boolean {
            val airingDateIsAdvanced by airingDateIsAdvanced.collectAsStateWithLifecycle()
            val airingDate by airingDate.collectAsStateWithLifecycle()
            val showingPreview =
                when (val data =
                    if (airingDateIsAdvanced) airingDate.second else airingDate.first) {
                    is AiringDate.Advanced -> (data.startDate != null) || (data.endDate != null)
                    is AiringDate.Basic -> (data.season != null)
                            || (data.seasonYear.toIntOrNull() != null)
                }
            return !showingPreview
        }

        @Composable
        override fun Content(state: SortFilterSection.ExpandedState, showDivider: Boolean) {
            var airingDateIsAdvanced by airingDateIsAdvanced.collectAsMutableStateWithLifecycle()
            var airingDate by airingDate.collectAsMutableStateWithLifecycle()
            var airingDateShown by airingDateShown.collectAsMutableStateWithLifecycle()
            AiringDateSection(
                expanded = { state.expandedState[id] == true },
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
            if (airingDateShown != null) {
                StartEndDateDialog(
                    shownForStartDate = airingDateShown,
                    onShownForStartDateChange = { airingDateShown = it },
                    onDateChange = ::onAiringDateChange,
                )
            }
        }
    }

    private val episodes = savedStateHandle.getMutableStateFlow<String, RangeData>(
        key = "episodes",
        initialValue = { RangeData(151) },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )
    protected val episodesSection = SortFilterSectionState.Range(
        title = Res.string.anime_media_filter_episodes_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_episodes_expand_content_description,
        initialData = RangeData(151),
        data = episodes,
        unboundedMax = true,
    )

    private fun onAiringDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.UTC)
                .date
        }

        airingDate.update {
            it.copy(
                second = if (start) {
                    it.second.copy(startDate = selectedDate)
                } else {
                    it.second.copy(endDate = selectedDate)
                }
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    val animeFilterParams = combineStates(
        mediaFilterParams,
        airingDate,
        airingDateIsAdvanced,
        formatIn,
        formatNotIn,
        episodes,
    ) {
        val baseParams = it[0] as MediaSearchFilterParams<SortType>
        val airingDate = it[1] as Pair<AiringDate.Basic, AiringDate.Advanced>
        val airingDateIsAdvanced = it[2] as Boolean
        val formatIn = it[3] as Set<MediaFormat>
        val formatNotIn = it[4] as Set<MediaFormat>
        val episodes = it[5] as RangeData
        baseParams.copy(
            airingDate = initialParams.year?.let { AiringDate.Basic(seasonYear = it.toString()) }
                ?: if (airingDateIsAdvanced) airingDate.second else airingDate.first,
            formatIn = formatIn.toList(),
            formatNotIn = formatNotIn.toList(),
            episodesRange = episodes,
        )
    }

    override val filterParams = animeFilterParams.debounceState(viewModelScope, 1.seconds)

    override val sections = aniListApi.authedUser
        .mapState(viewModelScope) { viewer ->
            listOfNotNull(
                sortSection,
                statusSection,
                formatSection,
                genreSection,
                tagSection,
                airingDateSection.takeIf { initialParams.airingDateEnabled },
                myListStatusSection.takeIf { viewer != null },
                averageScoreSection,
                episodesSection,
                sourceSection,
                licensedBySection,
                titleLanguageSection,
                advancedSection,
            )
        }

    override val state by lazy {
        SortFilterState(
            sections = sections,
            filterParams = filterParams,
            collapseOnClose = collapseOnClose,
        )
    }

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val featureOverrideProvider: FeatureOverrideProvider,
        private val json: Json,
        private val mediaGenresController: MediaGenresController,
        private val mediaLicensorsController: MediaLicensorsController,
        private val mediaTagsController: MediaTagsController,
        private val mediaDataSettings: MediaDataSettings,
        @Assisted private val savedStateHandle: ScopedSavedStateHandle,
    ) {
        fun <SortType> create(initialParams: InitialParams<SortType>)
                where SortType : SortOption, SortType : Enum<SortType> =
            AnimeSortFilterViewModel(
                aniListApi = aniListApi,
                featureOverrideProvider = featureOverrideProvider,
                json = json,
                mediaGenresController = mediaGenresController,
                mediaLicensorsController = mediaLicensorsController,
                mediaTagsController = mediaTagsController,
                mediaDataSettings = mediaDataSettings,
                initialParams = initialParams,
                savedStateHandle = savedStateHandle,
            )
    }
}
