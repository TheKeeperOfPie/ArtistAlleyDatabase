package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import androidx.paging.PagingData
import com.anilist.LicensorsQuery
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaSource
import com.anilist.type.MediaStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.filter.AnimeSettingsSortFilterController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.utils.filterOnIO
import com.thekeeperofpie.artistalleydatabase.compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortEntry
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortFilterSection
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterEntry
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    protected val scope: CoroutineScope,
    protected val aniListApi: AuthedAniListApi,
    settings: AnimeSettings,
    featureOverrideProvider: FeatureOverrideProvider,
    private val mediaTagsController: MediaTagsController,
    private val mediaGenresController: MediaGenresController,
    private val mediaLicensorsController: MediaLicensorsController,
    private val mediaType: MediaType,
) : AnimeSettingsSortFilterController<MediaSortFilterController.FilterParams<SortType>>(
    scope = scope,
    settings = settings,
    featureOverrideProvider = featureOverrideProvider
) {
    private var initialized = false
    protected var initialParams by mutableStateOf<ParamsType?>(null)

    val sortSection = SortFilterSection.Sort(
        enumClass = sortTypeEnumClass,
        defaultEnabled = null,
        headerTextRes = R.string.anime_media_filter_sort_label,
    )

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

    protected val myListStatusSection = SortFilterSection.Filter(
        titleRes = R.string.anime_media_filter_list_my_status_label,
        titleDropdownContentDescriptionRes = R.string.anime_media_filter_list_my_status_content_description,
        includeExcludeIconContentDescriptionRes = R.string.anime_media_filter_list_my_status_chip_state_content_description,
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

    private var tagsByCategory by mutableStateOf(emptyMap<String, TagSection>())
    protected val tagsByCategoryFiltered = snapshotFlow { tagsByCategory }
        .flatMapLatest { tags ->
            settings.showAdult.map { showAdult ->
                if (showAdult) return@map tags
                tags.values.mapNotNull { it.filter { it.isAdult == false } }
                    .associateBy { it.name }
                    .toSortedMap(String.CASE_INSENSITIVE_ORDER)
            }
        }
    var tagRank by mutableStateOf("0")
    var tagSearchQuery by mutableStateOf("")
    var tagShowWhenSpoiler by mutableStateOf(false)

    protected val tagSection = object : SortFilterSection.Custom("tag") {
        override fun showingPreview() = tagsByCategory.any {
            it.value.filter { it.state != FilterIncludeExcludeState.DEFAULT } != null
        }

        override fun clear() {
            tagsByCategory = tagsByCategory.mapValues {
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
                showMediaWithTagSpoiler = { tagShowWhenSpoiler },
                onShowMediaWithTagSpoilerChange = { tagShowWhenSpoiler = it },
                tags = { tagsByCategoryFiltered },
                onTagClick = { tagId ->
                    if (tagId != initialParams?.tagId) {
                        tagsByCategory = tagsByCategory.mapValues { (_, value) ->
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
        property = settings.showLessImportantTags,
    )

    protected val showSpoilerTagsSection = SortFilterSection.SwitchBySetting(
        titleRes = R.string.anime_media_filter_show_spoiler_tags,
        property = settings.showSpoilerTags,
    )

    protected val titleLanguageSection = SortFilterSection.Dropdown(
        labelTextRes = R.string.anime_media_filter_setting_title_language,
        values = AniListLanguageOption.values().toList(),
        valueToText = { stringResource(it.textRes) },
        property = settings.languageOptionMedia,
    )

    open val suggestionsSection: SortFilterSection? = null

    override val sections get() = internalSections

    // This is kept separate so children can access the set
    protected open var internalSections by mutableStateOf(emptyList<SortFilterSection>())

    init {
        scope.launch(CustomDispatchers.Main) {
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

    protected fun initialize(initialParams: InitialParams<SortType>) {
        if (initialized) return
        initialized = true
        if (initialParams.tagId != null) {
            tagRank = "60"
        }

        scope.launch(CustomDispatchers.Main) {
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

        scope.launch(CustomDispatchers.Main) {
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
                .collectLatest { tagsByCategory = it }
        }
    }

    fun <Entry : MediaStatusAware> filterMedia(
        result: PagingData<Entry>,
        transform: (Entry) -> MediaPreview,
    ) = combine(
        flowOf(result),
        snapshotFlow {
            val includedTags = tagsByCategory.values.flatMap {
                when (it) {
                    is TagSection.Category -> it.flatten()
                    is TagSection.Tag -> listOf(it)
                }
            }.filter { it.state == FilterIncludeExcludeState.INCLUDE }

            Triple(includedTags, tagShowWhenSpoiler, myListStatusSection.filterOptions)
        }
            .flowOn(CustomDispatchers.Main),
    ) { pagingData, triple ->
        val (includedTags, tagShowWhenSpoiler, listStatuses) = triple
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

            if (!tagShowWhenSpoiler && includedTags.isNotEmpty()) {
                return@filterOnIO includedTags.all { tag ->
                    media.tags?.find { it?.id.toString() == tag.id }
                        ?.isMediaSpoiler != true
                }
            }

            true
        }
    }

    interface InitialParams<SortType : SortOption> {
        val tagId: String?
        val genre: String?
        val year: Int?
    }

    data class FilterParams<SortType : SortOption>(
        val sort: List<SortEntry<SortType>>,
        val sortAscending: Boolean,
        val genres: List<FilterEntry<String>>,
        val tagsByCategory: Map<String, TagSection>,
        val tagRank: Int?,
        val statuses: List<FilterEntry<MediaStatus>>,
        val myListStatuses: List<FilterEntry<MediaListStatus>>,
        val theirListStatuses: List<FilterEntry<MediaListStatus>>?,
        val onList: Boolean?,
        val myScore: RangeData?,
        val theirScore: RangeData?,
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
