package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.filter
import com.anilist.MediaTagsQuery
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.mapLatestNotNull
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeSortFilterController<SortType : SortOption>(
    sortTypeEnumClass: KClass<SortType>,
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
) : SortFilterController {
    companion object {
        private const val TAG = "AnimeFilterController"

        fun <SortType : SortOption, MediaEntryType : AnimeMediaListRow.Entry<MediaType>, MediaType : MediaPreview> filterEntries(
            filterParams: FilterParams<SortType>,
            entries: List<MediaEntryType>,
            forceShowIgnored: Boolean = false,
        ): List<MediaEntryType> {
            var filteredEntries = entries

            filteredEntries = FilterIncludeExcludeState.applyFiltering(
                filterParams.statuses,
                filteredEntries,
                state = { it.state },
                key = { it.value },
                transform = { listOfNotNull(it.media.status) }
            )

            filteredEntries = FilterIncludeExcludeState.applyFiltering(
                filterParams.formats,
                filteredEntries,
                state = { it.state },
                key = { it.value },
                transform = { listOfNotNull(it.media.format) }
            )

            filteredEntries = FilterIncludeExcludeState.applyFiltering(
                filterParams.genres,
                filteredEntries,
                state = { it.state },
                key = { it.value },
                transform = { it.media.genres?.filterNotNull().orEmpty() }
            )

            val tagRank = filterParams.tagRank
            val transformIncludes: ((AnimeMediaListRow.Entry<*>) -> List<String>)? =
                if (tagRank == null) null else {
                    {
                        it.media.tags
                            ?.filterNotNull()
                            ?.filter { it.rank?.let { it >= tagRank } == true }
                            ?.map { it.id.toString() }
                            .orEmpty()
                    }
                }

            filteredEntries = FilterIncludeExcludeState.applyFiltering(
                filterParams.tagsByCategory.values.flatMap {
                    when (it) {
                        is TagSection.Category -> it.flatten()
                        is TagSection.Tag -> listOf(it)
                    }
                },
                filteredEntries,
                state = { it.state },
                key = { it.value.id.toString() },
                transform = { it.media.tags?.filterNotNull()?.map { it.id.toString() }.orEmpty() },
                transformIncludes = transformIncludes,
            )

            if (!filterParams.showAdult) {
                filteredEntries = filteredEntries.filterNot { it.media.isAdult ?: false }
            }

            if (!filterParams.showIgnored && !forceShowIgnored) {
                filteredEntries = filteredEntries.filterNot { it.ignored }
            }

            filteredEntries = when (val airingDate = filterParams.airingDate) {
                is AiringDate.Basic -> {
                    filteredEntries.filter {
                        val season = airingDate.season
                        val seasonYear = airingDate.seasonYear.toIntOrNull()
                        (seasonYear == null || it.media.seasonYear == seasonYear)
                                && (season == null || it.media.season == season)
                    }
                }
                is AiringDate.Advanced -> {
                    val startDate = airingDate.startDate
                    val endDate = airingDate.endDate

                    if (startDate == null && endDate == null) {
                        filteredEntries
                    } else {
                        fun List<MediaEntryType>.filterStartDate(
                            startDate: LocalDate
                        ) = filter {
                            val mediaStartDate = it.media.startDate
                            val mediaYear = mediaStartDate?.year
                            if (mediaYear == null) {
                                return@filter false
                            } else if (mediaYear > startDate.year) {
                                return@filter true
                            } else if (mediaYear < startDate.year) {
                                return@filter false
                            }

                            val mediaMonth = mediaStartDate.month
                            val mediaDayOfMonth = mediaStartDate.day

                            // TODO: Is this the correct behavior?
                            // If there's no month, match the media to avoid stripping expected result
                            if (mediaMonth == null) {
                                return@filter true
                            }

                            if (mediaMonth < startDate.monthValue) {
                                return@filter false
                            }

                            if (mediaMonth > startDate.monthValue) {
                                return@filter true
                            }

                            mediaDayOfMonth == null || mediaDayOfMonth >= startDate.dayOfMonth
                        }

                        fun List<MediaEntryType>.filterEndDate(
                            endDate: LocalDate
                        ) = filter {
                            val mediaStartDate = it.media.startDate
                            val mediaYear = mediaStartDate?.year
                            if (mediaYear == null) {
                                return@filter false
                            } else if (mediaYear > endDate.year) {
                                return@filter false
                            } else if (mediaYear < endDate.year) {
                                return@filter true
                            }

                            val mediaMonth = mediaStartDate.month
                            val mediaDayOfMonth = mediaStartDate.day

                            // TODO: Is this the correct behavior?
                            // If there's no month, match the media to avoid stripping expected result
                            if (mediaMonth == null) {
                                return@filter true
                            }

                            if (mediaMonth < endDate.monthValue) {
                                return@filter true
                            }

                            if (mediaMonth > endDate.monthValue) {
                                return@filter false
                            }

                            mediaDayOfMonth == null || mediaDayOfMonth <= endDate.dayOfMonth
                        }

                        if (startDate != null && endDate != null) {
                            filteredEntries.filterStartDate(startDate)
                                .filterEndDate(endDate)
                        } else if (startDate != null) {
                            filteredEntries.filterStartDate(startDate)
                        } else if (endDate != null) {
                            filteredEntries.filterEndDate(endDate)
                        } else {
                            filteredEntries
                        }
                    }
                }
            }

            val averageScore = filterParams.averageScoreRange
            val averageScoreStart = averageScore.startInt ?: 0
            val averageScoreEnd = averageScore.endInt
            if (averageScoreStart > 0) {
                filteredEntries = filteredEntries.filter {
                    it.media.averageScore.let { it != null && it >= averageScoreStart }
                }
            }
            if (averageScoreEnd != null) {
                filteredEntries = filteredEntries.filter {
                    it.media.averageScore.let { it != null && it <= averageScoreEnd }
                }
            }

            val episodes = filterParams.episodesRange
            val episodesStart = episodes.startInt ?: 0
            val episodesEnd = episodes.endInt
            if (episodesStart > 0) {
                filteredEntries = filteredEntries.filter {
                    it.media.episodes.let { it != null && it >= episodesStart }
                }
            }
            if (episodesEnd != null) {
                filteredEntries = filteredEntries.filter {
                    it.media.episodes.let { it != null && it <= episodesEnd }
                }
            }

            filteredEntries = FilterIncludeExcludeState.applyFiltering(
                filterParams.sources,
                filteredEntries,
                state = { it.state },
                key = { it.value },
                transform = { listOfNotNull(it.media.source) }
            )

            return filteredEntries
        }
    }

    var tagLongClickListener: (String) -> Unit = {}

    var initialParams by mutableStateOf<InitialParams<SortType>?>(null)
    override val state = SortFilterSection.ExpandedState()
    var mediaType by mutableStateOf(MediaType.ANIME)

    val sortSection = SortFilterSection.Sort(
        enumClass = sortTypeEnumClass,
        defaultEnabled = null,
        headerTextRes = R.string.anime_media_filter_sort_label,
    )

    val statusSection = SortFilterSection.Filter(
        titleRes = R.string.anime_media_filter_status_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_status_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_status_chip_state_content_description,
        values = listOf(
            MediaStatus.FINISHED,
            MediaStatus.RELEASING,
            MediaStatus.NOT_YET_RELEASED,
            MediaStatus.CANCELLED,
            MediaStatus.HIATUS,
        ),
        valueToText = { stringResource(it.value.toTextRes()) },
    )

    val listStatusSection = SortFilterSection.Filter(
        titleRes = R.string.anime_media_filter_list_status_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_list_status_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_list_status_chip_state_content_description,
        values = listOf(
            MediaListStatus.CURRENT,
            MediaListStatus.PLANNING,
            MediaListStatus.COMPLETED,
            MediaListStatus.DROPPED,
            MediaListStatus.PAUSED,
            MediaListStatus.REPEATING,
        ),
        valueToText = { stringResource(it.value.toTextRes(mediaType)) },
    )

    val formatSection = SortFilterSection.Filter(
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

    val genreSection = SortFilterSection.Filter<String>(
        titleRes = R.string.anime_media_filter_genre_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_genre_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_genre_chip_state_content_description,
        values = emptyList(),
        valueToText = { it.value },
    )

    val tagsByCategory = MutableStateFlow(emptyMap<String, TagSection>())
    private val tagsByCategoryFiltered = tagsByCategory.flatMapLatest { tags ->
        settings.showAdult.map { showAdult ->
            if (showAdult) return@map tags
            tags.values.mapNotNull {
                // Keep if previously selected (not DEFAULT)
                it.filter { it.state != FilterIncludeExcludeState.DEFAULT || it.isAdult != true }
            }
                .associateBy { it.name }
                .toSortedMap(String.CASE_INSENSITIVE_ORDER)
        }
    }
    var tagRank by mutableStateOf("0")

    val tagSection = object : SortFilterSection.Custom("tag") {
        @Composable
        override fun Content(state: ExpandedState) {
            TagSection(
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                tags = { tagsByCategoryFiltered.collectAsState(emptyMap()).value },
                onTagClick = { tagId ->
                    if (tagId != initialParams?.tagId) {
                        tagsByCategory.value = tagsByCategory.value
                            .mapValues { (_, value) ->
                                value.replace {
                                    it.takeUnless { it.id == tagId }
                                        ?: it.copy(state = it.state.next())
                                }
                            }
                    }
                },
                onTagLongClick = tagLongClickListener,
                tagRank = { tagRank },
                onTagRankChange = { tagRank = it },
            )
        }
    }

    var airingDate by mutableStateOf(AiringDate.Basic() to AiringDate.Advanced())
    var airingDateIsAdvanced by mutableStateOf(false)
    var airingDateShown by mutableStateOf<Boolean?>(null)

    val airingDateSection = object : SortFilterSection.Custom("airingDate") {
        @Composable
        override fun Content(state: ExpandedState) {
            AiringDateSection(
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                data = { if (airingDateIsAdvanced) airingDate.second else airingDate.first },
                onSeasonChange = {
                    airingDate = airingDate.copy(first = airingDate.first.copy(season = it))
                },
                onSeasonYearChange = {
                    airingDate = airingDate.copy(first = airingDate.first.copy(seasonYear = it))
                },
                onIsAdvancedToggle = { airingDateIsAdvanced = it },
                onRequestDatePicker = { airingDateShown = it },
                onDateChange = ::onAiringDateChange,
            )
        }
    }

    val onListSection = SortFilterSection.Filter(
        titleRes = R.string.anime_media_filter_on_list_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_on_list_dropdown_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_on_list_chip_state_content_description,
        values = listOf(true, false),
        valueToText = {
            stringResource(
                when (it.value) {
                    true -> R.string.anime_media_filter_on_list_on_list
                    false -> R.string.anime_media_filter_on_list_not_on_list
                }
            )
        },
        exclusive = true,
    )

    val averageScoreSection = SortFilterSection.Range(
        titleRes = R.string.anime_media_filter_average_score_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_average_score_expand_content_description,
        data = RangeData(100, hardMax = true),
    )

    val episodesSection = SortFilterSection.Range(
        titleRes = R.string.anime_media_filter_episodes_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_episodes_expand_content_description,
        data = RangeData(151),
        unboundedMax = true,
    )

    val sourceSection = SortFilterSection.Filter(
        titleRes = R.string.anime_media_filter_source_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_source_expand_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_source_chip_state_content_description,
        values = listOf(
            MediaSource.ORIGINAL,
            MediaSource.ANIME,
            MediaSource.COMIC,
            MediaSource.DOUJINSHI,
            MediaSource.GAME,
            MediaSource.LIGHT_NOVEL,
            MediaSource.LIVE_ACTION,
            MediaSource.MANGA,
            MediaSource.MULTIMEDIA_PROJECT,
            MediaSource.NOVEL,
            MediaSource.OTHER,
            MediaSource.PICTURE_BOOK,
            MediaSource.VIDEO_GAME,
            MediaSource.VISUAL_NOVEL,
            MediaSource.WEB_NOVEL,
        ),
        valueToText = { stringResource(it.value.toTextRes()) },
        exclusive = true,
    )

    val showAdultSection = SortFilterSection.Switch(
        titleRes = R.string.anime_media_filter_show_adult_content,
        enabled = false,
    )

    val collapseOnCloseSection = SortFilterSection.Switch(
        titleRes = R.string.anime_media_filter_show_adult_content,
        enabled = true,
    )

    val showIgnoredSection = SortFilterSection.Switch(
        titleRes = R.string.anime_media_filter_show_ignored,
        enabled = true,
    )

    val actionsSection = object : SortFilterSection.Custom("actions") {
        @Composable
        override fun Content(state: ExpandedState) {
            // TODO("Not yet implemented")
        }
    }

    override var sections by mutableStateOf(emptyList<SortFilterSection>())

    fun initialize(
        viewModel: ViewModel,
        refreshUptimeMillis: MutableStateFlow<*>,
        initialParams: InitialParams<SortType>,
        mediaType: MediaType,
        tagLongClickListener: (String) -> Unit = { /* TODO */ },
    ) {
        this.tagLongClickListener = tagLongClickListener
        this.mediaType = mediaType
        viewModel.viewModelScope.launch(CustomDispatchers.Main) {
            aniListApi.authedUser
                .mapLatest { viewer ->
                    listOfNotNull(
                        sortSection.apply { changeDefaultEnabled(initialParams.defaultSort) },
                        statusSection,
                        listStatusSection.takeIf { viewer != null },
                        formatSection,
                        genreSection,
                        tagSection,
                        airingDateSection.takeIf { initialParams.airingDateEnabled },
                        onListSection.takeIf { viewer != null && initialParams.onListEnabled }
                            ?.apply { exclusive = initialParams.onListExclusive },
                        averageScoreSection,
                        episodesSection,
                        sourceSection,
                        showAdultSection,
                        collapseOnCloseSection,
                        showIgnoredSection.takeIf { initialParams.showIgnoredEnabled },
                        actionsSection,
                    )
                }
                .collectLatest { sections = it }
        }

        viewModel.viewModelScope.launch(CustomDispatchers.Main) {
            refreshUptimeMillis
                .mapLatest {
                    FilterEntry.values(
                        values = aniListApi.genres()
                            .genreCollection
                            ?.filterNotNull()
                            .orEmpty(),
                        included = initialParams.genresIncluded,
                        excluded = initialParams.genresExcluded,
                    )
                }
                .catch { /* TODO: Error message */ }
                .take(1)
                .flowOn(CustomDispatchers.IO)
                .collectLatest {
                    genreSection.filterOptions = it
                }
        }

        viewModel.viewModelScope.launch(CustomDispatchers.Main) {
            refreshUptimeMillis
                .mapLatestNotNull {
                    withContext(CustomDispatchers.IO) {
                        try {
                            aniListApi.tags().mediaTagCollection
                                ?.filterNotNull()
                                ?.let(::buildTagSections)
                                ?.run {
                                    val tagsIncluded = initialParams.tagsIncluded
                                    val tagsExcluded = initialParams.tagsExcluded
                                    if (tagsIncluded.isEmpty() && tagsExcluded.isEmpty()
                                        && initialParams.tagId == null
                                    ) {
                                        return@run this
                                    }

                                    toMutableMap().apply {
                                        replaceAll { _, section ->
                                            section.replace {
                                                if (it.id == initialParams.tagId) {
                                                    it.copy(
                                                        state = FilterIncludeExcludeState.INCLUDE,
                                                        clickable = false
                                                    )
                                                } else {
                                                    it.copy(
                                                        state = FilterIncludeExcludeState.toState(
                                                            it.id,
                                                            included = tagsIncluded,
                                                            excluded = tagsExcluded
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                        } catch (e: Exception) {
                            Log.d(TAG, "Error loading tags", e)
                            null
                        }
                    }
                }
                .take(1)
                .collectLatest(tagsByCategory::emit)
        }
    }

    /**
     * Categories are provided from the API in the form of "Parent-Child". This un-flattens the
     * tag list into a tree of sections, separated by the "-" dash.
     */
    private fun buildTagSections(
        tags: List<MediaTagsQuery.Data.MediaTagCollection>,
    ): Map<String, TagSection> {
        val sections = mutableMapOf<String, Any>()
        tags.forEach {
            var categories = it.category?.split('-')

            // Manually handle the "Sci-Fi" category, which contains a dash, but shouldn't be split
            if (categories != null) {
                val sciIndex = categories.indexOf("Sci")
                if (sciIndex >= 0) {
                    val hasFi = categories.getOrNull(sciIndex + 1) == "Fi"
                    if (hasFi) {
                        categories = categories.toMutableList().apply {
                            removeAt(sciIndex + 1)
                            set(sciIndex, "Sci-Fi")
                        }
                    }
                }
            }

            var currentCategory: TagSection.Category.Builder? = null
            categories?.forEach {
                currentCategory = if (currentCategory == null) {
                    sections.getOrPut(it) { TagSection.Category.Builder(it) }
                            as TagSection.Category.Builder
                } else {
                    (currentCategory as TagSection.Category.Builder).getOrPutCategory(it)
                }
            }

            if (currentCategory == null) {
                sections[it.name] = TagSection.Tag(it)
            } else {
                currentCategory!!.addChild(it)
            }
        }

        return sections.mapValues { (_, value) ->
            when (value) {
                is TagSection.Category.Builder -> value.build()
                is TagSection.Tag -> value
                else -> throw IllegalStateException("Unexpected value $value")
            }
        }
    }

    fun filterParams() =
        combine(
            snapshotFlow {
                FilterParams(
                    sort = sortSection.sortOptions,
                    sortAscending = sortSection.sortAscending,
                    genres = genreSection.filterOptions,
                    tagsByCategory = emptyMap(),
                    tagRank = tagRank.toIntOrNull()?.coerceIn(0, 100),
                    statuses = statusSection.filterOptions,
                    listStatuses = listStatusSection.filterOptions,
                    formats = formatSection.filterOptions,
                    averageScoreRange = averageScoreSection.data,
                    episodesRange = episodesSection.data,
                    onListOptions = onListSection.filterOptions,
                    showAdult = showAdultSection.enabled,
                    showIgnored = showIgnoredSection.enabled,
                    airingDate = if (airingDateIsAdvanced) airingDate.second else airingDate.first,
                    sources = sourceSection.filterOptions,
                )
            },
            tagsByCategory,
            ::Pair
        )
            .flowOn(CustomDispatchers.Main)
            .mapLatest { (params, tags) ->
                params.copy(tagsByCategory = tags)
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

    fun <Entry : MediaStatusAware> filterMedia(
        result: PagingData<Entry>,
        transform: (Entry) -> MediaPreview,
    ) = combine(
        flowOf(result),
        snapshotFlow { showIgnoredSection.enabled to listStatusSection.filterOptions }
            .flowOn(CustomDispatchers.Main),
    ) { pagingData, paramsPair ->
        val (showIgnored, listStatuses) = paramsPair
        val includes = listStatuses
            .filter { it.state == FilterIncludeExcludeState.INCLUDE }
            .map { it.value }
        val excludes = listStatuses
            .filter { it.state == FilterIncludeExcludeState.EXCLUDE }
            .map { it.value }
        pagingData.filter {
            val media = transform(it)
            val listStatus = media.mediaListEntry?.status
            if (excludes.isNotEmpty() && excludes.contains(listStatus)) {
                return@filter false
            }

            if (includes.isNotEmpty() && !includes.contains(listStatus)) {
                return@filter false
            }

            if (showIgnored) true else !it.ignored
        }
    }

    override fun collapseOnClose() = collapseOnCloseSection.enabled

    data class InitialParams<SortType : SortOption>(
        val tagId: String? = null,
        val tagsIncluded: Set<String> = emptySet(),
        val tagsExcluded: Set<String> = emptySet(),
        val genresIncluded: Set<String> = emptySet(),
        val genresExcluded: Set<String> = emptySet(),
        val airingDateEnabled: Boolean = true,
        val onListEnabled: Boolean = true,
        val showIgnoredEnabled: Boolean = true,
        val onListExclusive: Boolean = false,
        val defaultSort: SortType?,
    )

    data class FilterParams<SortType : SortOption>(
        val sort: List<SortEntry<SortType>>,
        val sortAscending: Boolean,
        val genres: List<FilterEntry<String>>,
        val tagsByCategory: Map<String, TagSection>,
        val tagRank: Int?,
        val statuses: List<FilterEntry<MediaStatus>>,
        val listStatuses: List<FilterEntry<MediaListStatus>>,
        val formats: List<FilterEntry<MediaFormat>>,
        val averageScoreRange: RangeData,
        val episodesRange: RangeData,
        val onListOptions: List<FilterEntry<Boolean>>,
        val showAdult: Boolean,
        val showIgnored: Boolean,
        val airingDate: AiringDate,
        val sources: List<FilterEntry<MediaSource>>,
    )
}
