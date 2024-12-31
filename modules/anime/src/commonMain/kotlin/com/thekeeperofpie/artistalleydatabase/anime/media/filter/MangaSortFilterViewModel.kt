package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
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
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.AiringDate
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.AiringDateAdvancedSection
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.ui.StartEndDateDialog
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.debounceState
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import com.thekeeperofpie.artistalleydatabase.utils_compose.ScopedSavedStateHandle
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.CustomFilterSection
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSection.ExpandedState
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

open class MangaSortFilterViewModel<SortType>(
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

    // TODO: Can in/notIn be moved up to MediaSortFilterViewModel?
    private val formatIn = savedStateHandle.getMutableStateFlow<String, Set<MediaFormat>>(
        key = "formatIn",
        initialValue = { emptySet() },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )
    private val formatNotIn = savedStateHandle.getMutableStateFlow<String, Set<MediaFormat>>(
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
                MediaFormat.MANGA,
                MediaFormat.NOVEL,
                MediaFormat.ONE_SHOT,
            )
        ),
        filterIn = formatIn,
        filterNotIn = formatNotIn,
        valueToText = { stringResource(it.toTextRes()) },
    )

    private var releaseDate = savedStateHandle.getMutableStateFlow(
        key = "releaseDate",
        initialValue = { AiringDate.Advanced() },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )
    private var releaseDateShown = savedStateHandle.getMutableStateFlow<String, Boolean?>(
        key = "releaseDateShown",
        initialValue = { null },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )
    protected val releaseDateSection = object : SortFilterSectionState.Custom("releaseDate") {
        override fun clear() {
            releaseDate.value = AiringDate.Advanced()
            releaseDateShown.value = null
        }

        @Composable
        override fun isDefault() =
            releaseDate.collectAsStateWithLifecycle().value.summaryText() == null

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            val releaseDate by releaseDate.collectAsStateWithLifecycle()
            var releaseDateShown by releaseDateShown.collectAsMutableStateWithLifecycle()
            val expanded = state.expandedState[id] == true
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
            if (releaseDateShown != null) {
                StartEndDateDialog(
                    shownForStartDate = releaseDateShown,
                    onShownForStartDateChange = { releaseDateShown = it },
                    onDateChange = ::onReleaseDateChange,
                )
            }
        }
    }

    // TODO: Fix volumes/chapters range search
    private val volumes = savedStateHandle.getMutableStateFlow<String, RangeData>(
        key = "volumes",
        initialValue = { RangeData(151) },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )
    protected val volumesSection = SortFilterSectionState.Range(
        title = Res.string.anime_media_filter_volumes_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_volumes_expand_content_description,
        initialData = RangeData(151),
        data = volumes,
        unboundedMax = true,
    )

    private val chapters = savedStateHandle.getMutableStateFlow<String, RangeData>(
        key = "chapters",
        initialValue = { RangeData(151) },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )
    protected val chaptersSection = SortFilterSectionState.Range(
        title = Res.string.anime_media_filter_chapters_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_chapters_expand_content_description,
        initialData = RangeData(151),
        data = chapters,
        unboundedMax = true,
    )

    @Suppress("UNCHECKED_CAST")
    val mangaFilterParams = combineStates(
        mediaFilterParams,
        formatIn,
        formatNotIn,
        volumes,
        chapters,
    ) {
        val baseParams = it[0] as MediaSearchFilterParams<SortType>
        val formatIn = it[1] as Set<MediaFormat>
        val formatNotIn = it[2] as Set<MediaFormat>
        val volumes = it[3] as RangeData
        val chapters = it[4] as RangeData
        baseParams.copy(
            formatIn = formatIn.toList(),
            formatNotIn = formatNotIn.toList(),
            volumesRange = volumes,
            chaptersRange = chapters,
        )
    }

    fun onReleaseDateChange(start: Boolean, selectedMillis: Long?) {
        // Selected value is in UTC
        val selectedDate = selectedMillis?.let {
            Instant.fromEpochMilliseconds(it)
                .toLocalDateTime(TimeZone.UTC)
                .date
        }

        releaseDate.update {
            if (start) {
                it.copy(startDate = selectedDate)
            } else {
                it.copy(endDate = selectedDate)
            }
        }
    }

    override val filterParams = mangaFilterParams.debounceState(viewModelScope, 1.seconds)

    override val sections = aniListApi.authedUser
        .mapState(viewModelScope) { viewer ->
            listOfNotNull(
                sortSection,
                statusSection,
                formatSection,
                genreSection,
                tagSection,
                releaseDateSection.takeIf { initialParams.airingDateEnabled },
                myListStatusSection.takeIf { viewer != null },
                averageScoreSection,
                volumesSection,
                chaptersSection,
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
            MangaSortFilterViewModel(
                aniListApi = aniListApi,
                featureOverrideProvider = featureOverrideProvider,
                json = json,
                mediaGenresController = mediaGenresController,
                mediaLicensorsController = mediaLicensorsController,
                mediaTagsController = mediaTagsController,
                mediaDataSettings = mediaDataSettings,
                initialParams = initialParams,
                savedStateHandle = savedStateHandle
            )
    }
}
