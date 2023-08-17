package com.thekeeperofpie.artistalleydatabase.anime.media.filter

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
import com.anilist.LicensorsQuery
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.utils.filterOnIO
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
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
abstract class MediaSortFilterController<SortType : SortOption, ParamsType : MediaSortFilterController.InitialParams<SortType>>(
    sortTypeEnumClass: KClass<SortType>,
    protected val aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val mediaTagsController: MediaTagsController,
    private val mediaGenresController: MediaGenresController,
    private val mediaLicensorsController: MediaLicensorsController,
    private val mediaType: MediaType,
) : SortFilterController(settings, featureOverrideProvider) {
    private var initialized = false
    protected var initialParams by mutableStateOf<ParamsType?>(null)

    protected val sortSection = SortFilterSection.Sort(
        enumClass = sortTypeEnumClass,
        defaultEnabled = null,
        headerTextRes = R.string.anime_media_filter_sort_label,
    ).apply {
        sortOptions = if (mediaType == MediaType.ANIME) {
            sortOptions.filter { it.value != MediaSortOption.VOLUMES }
                .filter { it.value != MediaSortOption.CHAPTERS }
        } else {
            sortOptions.filter { it.value != MediaSortOption.EPISODES }

        }
    }

    protected val statusSection = SortFilterSection.Filter(
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

    protected val listStatusSection = SortFilterSection.Filter(
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
            // Null is used to represent "on list", to avoid having a separate section
            null,
        ),
        valueToText = {
            stringResource(
                if (it.value == null) {
                    R.string.anime_media_filter_list_status_on_list
                } else {
                    it.value.toTextRes(mediaType)
                }
            )
        },
    )

    protected val genreSection = SortFilterSection.Filter(
        titleRes = R.string.anime_media_filter_genre_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_genre_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_genre_chip_state_content_description,
        values = emptyList(),
        valueToText = { it.value },
    )

    private val tagsByCategory = MutableStateFlow(emptyMap<String, TagSection>())
    protected val tagsByCategoryFiltered = tagsByCategory.flatMapLatest { tags ->
        settings.showAdult.map { showAdult ->
            if (showAdult) return@map tags
            tags.values.mapNotNull { it.filter { it.isAdult == false } }
                .associateBy { it.name }
                .toSortedMap(String.CASE_INSENSITIVE_ORDER)
        }
    }
    var tagRank by mutableStateOf("0")
    var tagSearchQuery by mutableStateOf("")

    protected val tagSection = object : SortFilterSection.Custom("tag") {
        override fun showingPreview() = true

        override fun clear() {
            tagsByCategory.value = tagsByCategory.value.mapValues {
                it.value.replace {
                    if (it.id == initialParams?.tagId) {
                        it
                    } else {
                        it.copy(state = FilterIncludeExcludeState.DEFAULT)
                    }
                }
            }
            tagRank = "0"
            tagSearchQuery = ""
        }

        @Composable
        override fun Content(state: ExpandedState, showDivider: Boolean) {
            val tagsByCategoryFiltered by tagsByCategoryFiltered.collectAsState(emptyMap())
            TagSection(
                expanded = { state.expandedState[id] ?: false },
                onExpandedChange = { state.expandedState[id] = it },
                tags = { tagsByCategoryFiltered },
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
                tagRank = { tagRank },
                onTagRankChange = { tagRank = it },
                query = tagSearchQuery,
                onQueryChange = { tagSearchQuery = it },
                showDivider = showDivider,
            )
        }
    }

    protected val averageScoreSection = SortFilterSection.Range(
        titleRes = R.string.anime_media_filter_average_score_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_average_score_expand_content_description,
        initialData = RangeData(100, hardMax = true),
    )

    protected val sourceSection = SortFilterSection.Filter(
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
        selectionMethod = SortFilterSection.Filter.SelectionMethod.ONLY_INCLUDE,
    )

    protected val licensedBySection =
        SortFilterSection.Group<SortFilterSection.Filter<LicensorsQuery.Data.ExternalLinkSourceCollection>>(
            titleRes = R.string.anime_media_filter_licensed_by_label,
            titleDropdownContentDescriptionRes = R.string.anime_media_filter_licensed_by_content_description,
            children = emptyList(),
            onlyShowChildIfSingle = true,
        )

    protected val showLessImportantTagsSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_media_filter_show_less_important_tags,
        settings = settings,
        property = { it.showLessImportantTags },
    )

    protected val showSpoilerTagsSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_media_filter_show_spoiler_tags,
        settings = settings,
        property = { it.showSpoilerTags },
    )

    override var sections by mutableStateOf(emptyList<SortFilterSection>())

    protected fun initialize(
        viewModel: ViewModel,
        refreshUptimeMillis: MutableStateFlow<*>,
        initialParams: InitialParams<SortType>,
    ) {
        if (initialized) return
        initialized = true
        if (initialParams.tagId != null) {
            tagRank = "60"
        }

        viewModel.viewModelScope.launch(CustomDispatchers.Main) {
            mediaGenresController.genres
                .flatMapLatest { genres ->
                    settings.showAdult.mapLatest { showAdult ->
                        FilterEntry.values(genres.filter { showAdult || it != "Hentai" })
                            .map {
                                it.transformIf(it.value == initialParams.genre) {
                                    copy(
                                        state = FilterIncludeExcludeState.INCLUDE,
                                        clickable = false,
                                    )
                                }
                            }
                    }
                }
                .catch { /* TODO: Error message */ }
                .flowOn(CustomDispatchers.IO)
                .collectLatest(genreSection::setDefaultValues)
        }

        viewModel.viewModelScope.launch(CustomDispatchers.Main) {
            mediaTagsController.tags
                .map {
                    it.mapValues { (_, section) ->
                        section.replace { tag ->
                            tag.transformIf(tag.id == initialParams.tagId) {
                                copy(
                                    state = FilterIncludeExcludeState.INCLUDE,
                                    clickable = false,
                                )
                            }
                        }
                    }
                }
                .collectLatest(tagsByCategory::emit)
        }

        viewModel.viewModelScope.launch(CustomDispatchers.Main) {
            val licensors = if (mediaType == MediaType.ANIME) {
                mediaLicensorsController.anime
            } else {
                mediaLicensorsController.manga
            }
            licensors.mapLatest { languageAndSites ->
                languageAndSites.map {
                    SortFilterSection.Filter(
                        id = "licensedBy-${it.language}",
                        title = {
                            it.language ?: if (languageAndSites.size == 1) {
                                stringResource(R.string.anime_media_filter_licensed_by_label)
                            } else {
                                stringResource(R.string.anime_media_filter_licensed_by_general_label)
                            }
                        },
                        titleDropdownContentDescriptionRes = R.string.anime_media_filter_licensed_by_content_description,
                        // TODO: Content description should include site?
                        includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_licensed_by_chip_icon_content_description,
                        values = it.sites,
                        valueToText = { it.value.site },
                        valueToImage = { it.value.icon },
                        selectionMethod = SortFilterSection.Filter.SelectionMethod.ONLY_INCLUDE,
                    )
                }
            }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { licensedBySection.children = it }
        }
    }

    fun <Entry : MediaStatusAware> filterMedia(
        result: PagingData<Entry>,
        transform: (Entry) -> MediaPreview,
    ) = combine(
        flowOf(result),
        settings.showAdult,
        settings.showIgnored,
        snapshotFlow { listStatusSection.filterOptions }
            .flowOn(CustomDispatchers.Main),
    ) { pagingData, showAdult, showIgnored, listStatuses ->
        val includes = listStatuses
            .filter { it.state == FilterIncludeExcludeState.INCLUDE }
            .mapNotNull { it.value }
        val excludes = listStatuses
            .filter { it.state == FilterIncludeExcludeState.EXCLUDE }
            .mapNotNull { it.value }
        pagingData.filterOnIO {
            val media = transform(it)
            val listStatus = media.mediaListEntry?.status
            if (excludes.isNotEmpty() && excludes.contains(listStatus)) {
                return@filterOnIO false
            }

            if (includes.isNotEmpty() && !includes.contains(listStatus)) {
                return@filterOnIO false
            }

            if (!showAdult && media.isAdult != false) {
                return@filterOnIO false
            }

            if (showIgnored) true else !it.ignored
        }
    }

    interface InitialParams<SortType : SortOption> {
        val tagId: String?
        val genre: String?
    }

    data class FilterParams<SortType : SortOption>(
        val sort: List<SortEntry<SortType>>,
        val sortAscending: Boolean,
        val genres: List<FilterEntry<String>>,
        val tagsByCategory: Map<String, TagSection>,
        val tagRank: Int?,
        val statuses: List<FilterEntry<MediaStatus>>,
        val listStatuses: List<FilterEntry<MediaListStatus>>,
        val onList: Boolean?,
        val formats: List<FilterEntry<MediaFormat>>,
        val averageScoreRange: RangeData,
        val episodesRange: RangeData?,
        val volumesRange: RangeData?,
        val chaptersRange: RangeData?,
        val showAdult: Boolean,
        val showIgnored: Boolean,
        val airingDate: AiringDate,
        val sources: List<FilterEntry<MediaSource>>,
        val licensedBy: List<FilterEntry<LicensorsQuery.Data.ExternalLinkSourceCollection>>,
    )
}
