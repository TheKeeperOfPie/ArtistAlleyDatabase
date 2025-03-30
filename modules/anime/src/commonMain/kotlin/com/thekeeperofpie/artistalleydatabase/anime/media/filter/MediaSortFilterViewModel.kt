package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_average_score_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_average_score_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_genre_chip_state_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_genre_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_genre_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_licensed_by_chip_icon_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_licensed_by_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_licensed_by_general_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_licensed_by_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_list_my_status_chip_state_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_list_my_status_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_list_my_status_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_list_status_on_list
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_setting_title_language
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_show_less_important_tags
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_show_spoiler_tags
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_sort_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_chip_state_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_expand_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_source_label
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_status_chip_state_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_status_content_description
import artistalleydatabase.modules.anime.generated.resources.anime_media_filter_status_label
import com.anilist.data.LicensorsQuery
import com.anilist.data.fragment.MediaPreview
import com.anilist.data.type.MediaListStatus
import com.anilist.data.type.MediaSource
import com.anilist.data.type.MediaStatus
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaDataSortFilterViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.data.filter.MediaSearchFilterParams
import com.thekeeperofpie.artistalleydatabase.anime.media.data.toTextRes
import com.thekeeperofpie.artistalleydatabase.utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.mapState
import com.thekeeperofpie.artistalleydatabase.utils_compose.collectAsMutableStateWithLifecycle
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.RangeData
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterExpandedState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterSectionState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortFilterState
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.getMutableStateFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.filterOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
abstract class MediaSortFilterViewModel<SortType>(
    aniListApi: AuthedAniListApi,
    featureOverrideProvider: FeatureOverrideProvider,
    json: Json,
    mediaGenresController: MediaGenresController,
    mediaLicensorsController: MediaLicensorsController,
    private val mediaTagsController: MediaTagsController,
    mediaDataSettings: MediaDataSettings,
    initialParams: InitialParams<SortType>,
    savedStateHandle: SavedStateHandle,
    sortOptions: MutableStateFlow<List<SortType>>? = null,
) : MediaDataSortFilterViewModel(
    featureOverrideProvider = featureOverrideProvider,
    settings = mediaDataSettings,
    showHideIgnored = initialParams.showHideIgnored,
) where SortType : SortOption, SortType : Enum<SortType> {
    protected val sortOption =
        savedStateHandle.getMutableStateFlow<String, SortType>(
            "sortOption",
            { initialParams.defaultSort },
            // TODO: Find a better serialization method
            serialize = { it.name },
            deserialize = { java.lang.Enum.valueOf<SortType>(initialParams.sortClass.java, it) },
        )
    protected val sortAscending =
        savedStateHandle.getMutableStateFlow<Boolean>("sortAscending", false)
    protected val sortSection = SortFilterSectionState.Sort(
        headerText = Res.string.anime_media_filter_sort_label,
        defaultSort = initialParams.defaultSort,
        sortOptions = sortOptions
            ?: MutableStateFlow(initialParams.sortClass.java.enumConstants?.toList().orEmpty()),
        sortAscending = sortAscending,
        sortOption = sortOption,
    )

    protected val statusIn = savedStateHandle.getMutableStateFlow<List<String>, Set<MediaStatus>>(
        key = "statusIn",
        initialValue = { emptySet() },
        serialize = { it.map { it.name } },
        deserialize = { it.map { MediaStatus.valueOf(it) }.toSet() },
    )
    protected val statusNotIn =
        savedStateHandle.getMutableStateFlow<List<String>, Set<MediaStatus>>(
            key = "statusNotIn",
            initialValue = { emptySet() },
            serialize = { it.map { it.name } },
            deserialize = { it.map { MediaStatus.valueOf(it) }.toSet() },
        )
    protected val statusSection = SortFilterSectionState.Filter(
        title = Res.string.anime_media_filter_status_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_status_content_description,
        includeExcludeIconContentDescription = Res.string.anime_media_filter_status_chip_state_content_description,
        options = MutableStateFlow(
            listOf(
                MediaStatus.FINISHED,
                MediaStatus.RELEASING,
                MediaStatus.NOT_YET_RELEASED,
                MediaStatus.CANCELLED,
                MediaStatus.HIATUS,
            )
        ),
        filterIn = statusIn,
        filterNotIn = statusNotIn,
        valueToText = { stringResource(it.toTextRes()) },
    )

    protected val myListStatusIn =
        savedStateHandle.getMutableStateFlow<List<String>, Set<MediaListStatus?>>(
            key = "myListStatusIn",
            initialValue = { setOfNotNull(initialParams.mediaListStatus) },
            serialize = { it.map { it?.name.toString() } },
            deserialize = {
                it.map { if (it == "null") null else MediaListStatus.valueOf(it) }
                    .toSet()
            },
        )
    protected val myListStatusNotIn =
        savedStateHandle.getMutableStateFlow<List<String>, Set<MediaListStatus?>>(
            key = "myListStatusNotIn",
            initialValue = { emptySet() },
            serialize = { it.map { it?.name.toString() } },
            deserialize = {
                it.map { if (it == "null") null else MediaListStatus.valueOf(it) }
                    .toSet()
            },
        )
    protected val myListStatusSection = SortFilterSectionState.Filter(
        title = Res.string.anime_media_filter_list_my_status_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_list_my_status_content_description,
        includeExcludeIconContentDescription = Res.string.anime_media_filter_list_my_status_chip_state_content_description,
        options = MutableStateFlow(
            listOf(
                MediaListStatus.CURRENT,
                MediaListStatus.PLANNING,
                MediaListStatus.COMPLETED,
                MediaListStatus.DROPPED,
                MediaListStatus.PAUSED,
                MediaListStatus.REPEATING,
            ).let {
                if (initialParams.onListEnabled) {
                    // Null is used to represent "on list", to avoid having a separate section
                    it + null
                } else {
                    it
                }
            }),
        lockedFilterIn = setOfNotNull(initialParams.mediaListStatus),
        filterIn = myListStatusIn,
        filterNotIn = myListStatusNotIn,
        valueToText = {
            stringResource(
                it?.toTextRes(initialParams.mediaType)
                    ?: Res.string.anime_media_filter_list_status_on_list
            )
        },
    )

    private val genreIn =
        savedStateHandle.getMutableStateFlow<List<String>, Set<String>>(
            key = "genreIn",
            initialValue = { emptySet() },
            serialize = { it.toList() },
            deserialize = { it.toSet() },
        )
    private val genreNotIn =
        savedStateHandle.getMutableStateFlow<List<String>, Set<String>>(
            key = "genreNotIn",
            initialValue = { emptySet() },
            serialize = { it.toList() },
            deserialize = { it.toSet() },
        )
    protected val genreSection = SortFilterSectionState.Filter(
        title = Res.string.anime_media_filter_genre_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_genre_content_description,
        includeExcludeIconContentDescription = Res.string.anime_media_filter_genre_chip_state_content_description,
        options = mediaGenresController.genres,
        lockedFilterIn = setOfNotNull(initialParams.genre),
        filterIn = genreIn,
        filterNotIn = genreNotIn,
        valueToText = { it },
    )

    private val tagIdIn =
        savedStateHandle.getMutableStateFlow<List<String>, Set<String>>(
            key = "tagIdIn",
            initialValue = { emptySet() },
            serialize = { it.toList() },
            deserialize = { it.toSet() },
        )
    private val tagIdNotIn =
        savedStateHandle.getMutableStateFlow<List<String>, Set<String>>(
            key = "tagIdNotIn",
            initialValue = { emptySet() },
            serialize = { it.toList() },
            deserialize = { it.toSet() },
        )
    private val tagIdsLockedIn = setOfNotNull(initialParams.tagId)
    private val tagRank = savedStateHandle.getMutableStateFlow(
        "tagRank",
        if (initialParams.tagId == null) "0" else "60",
    )
    private val tagSearchQuery = savedStateHandle.getMutableStateFlow("tagSearchQuery", "")
    val tagShowWhenSpoiler = savedStateHandle.getMutableStateFlow("tagShowWhenSpoiler", false)
    protected val tagSection = object : SortFilterSectionState.Custom("tag") {
        override fun clear() {
            tagIdIn.value = tagIdsLockedIn
            tagIdNotIn.value = emptySet()
            tagRank.value = "0"
            tagSearchQuery.value = ""
        }

        @Composable
        override fun isDefault() = tagIdIn.collectAsState().value == tagIdsLockedIn
                && tagIdNotIn.collectAsState().value.isEmpty()
                && tagRank.collectAsState().value == "0"
                && tagSearchQuery.collectAsState().value.isEmpty()

        @Composable
        override fun Content(state: SortFilterExpandedState, showDivider: Boolean) {
            val tags by mediaTagsController.tags.collectAsStateWithLifecycle()
            var tagIn by tagIdIn.collectAsMutableStateWithLifecycle()
            var tagNotIn by tagIdNotIn.collectAsMutableStateWithLifecycle()
            var tagRank by tagRank.collectAsMutableStateWithLifecycle()
            var tagSearchQuery by tagSearchQuery.collectAsMutableStateWithLifecycle()
            var tagShowWhenSpoiler by tagShowWhenSpoiler.collectAsMutableStateWithLifecycle()
            TagSection(
                expanded = { state.expandedState[id] == true },
                onExpandedChange = { state.expandedState[id] = it },
                showMediaWithTagSpoiler = { tagShowWhenSpoiler },
                onShowMediaWithTagSpoilerChange = { tagShowWhenSpoiler = it },
                tags = tags,
                tagIdIn = tagIn,
                tagIdNotIn = tagNotIn,
                disabledOptions = tagIdsLockedIn,
                onTagClick = { tagId ->
                    if (tagId in tagIdsLockedIn) return@TagSection
                    if (tagIn.contains(tagId)) {
                        tagIn -= tagId
                        tagNotIn += tagId
                    } else if (tagNotIn.contains(tagId)) {
                        tagNotIn -= tagId
                    } else {
                        tagIn += tagId
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

    // TODO: Set min to 1 when max is not 100, to ignore media without a rating
    private val averageScore = savedStateHandle.getMutableStateFlow<String, RangeData>(
        key = "averageScore",
        initialValue = { RangeData(100, hardMax = true) },
        serialize = json::encodeToString,
        deserialize = json::decodeFromString,
    )
    protected val averageScoreSection = SortFilterSectionState.Range(
        title = Res.string.anime_media_filter_average_score_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_average_score_expand_content_description,
        initialData = RangeData(100, hardMax = true),
        data = averageScore,
    )

    private val sourceIn =
        savedStateHandle.getMutableStateFlow<String, Set<MediaSource?>>(
            key = "sourceIn",
            initialValue = { emptySet() },
            serialize = json::encodeToString,
            deserialize = json::decodeFromString,
        )
    protected val sourceSection = SortFilterSectionState.Filter(
        title = Res.string.anime_media_filter_source_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_source_expand_content_description,
        includeExcludeIconContentDescription = Res.string.anime_media_filter_source_chip_state_content_description,
        options = MutableStateFlow(
            listOf(
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
            )
        ),
        filterIn = sourceIn,
        filterNotIn = MutableStateFlow(emptySet()),
        valueToText = { stringResource(it.toTextRes()) },
        selectionMethod = SortFilterSectionState.Filter.SelectionMethod.ONLY_INCLUDE,
    )

    private val licensedByIn =
        savedStateHandle.getMutableStateFlow<String, Set<LicensorsQuery.Data.ExternalLinkSourceCollection>>(
            key = "licensedByIn",
            initialValue = { emptySet() },
            serialize = json::encodeToString,
            deserialize = json::decodeFromString,
        )
    private val licensedByChildren = if (initialParams.mediaType == MediaType.ANIME) {
        mediaLicensorsController.anime
    } else {
        mediaLicensorsController.manga
    }.mapState(viewModelScope) { languageAndSites ->
        languageAndSites.map {
            SortFilterSectionState.Filter<LicensorsQuery.Data.ExternalLinkSourceCollection>(
                id = "licensedBy-${it.language}",
                title = @Composable {
                    it.language ?: if (languageAndSites.size == 1) {
                        stringResource(Res.string.anime_media_filter_licensed_by_label)
                    } else {
                        stringResource(Res.string.anime_media_filter_licensed_by_general_label)
                    }
                },
                titleDropdownContentDescription = Res.string.anime_media_filter_licensed_by_content_description,
                // TODO: Content description should include site?
                includeExcludeIconContentDescription = Res.string.anime_media_filter_licensed_by_chip_icon_content_description,
                options = MutableStateFlow(it.sites),
                filterIn = licensedByIn,
                filterNotIn = MutableStateFlow(emptySet()),
                valueToText = { it.site },
                valueToImage = { value, enabled -> value.icon },
                selectionMethod = SortFilterSectionState.Filter.SelectionMethod.ONLY_INCLUDE,
            )
        }
    }
    protected val licensedBySection = SortFilterSectionState.Group(
        title = Res.string.anime_media_filter_licensed_by_label,
        titleDropdownContentDescription = Res.string.anime_media_filter_licensed_by_content_description,
        children = licensedByChildren,
        onlyShowChildIfSingle = true,
    )

    protected val titleLanguageSection = SortFilterSectionState.Dropdown(
        labelText = Res.string.anime_media_filter_setting_title_language,
        values = AniListLanguageOption.entries.toList(),
        valueToText = { stringResource(it.textRes) },
        property = mediaDataSettings.languageOptionMedia,
    )

    private val showLessImportantTagsSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.anime_media_filter_show_less_important_tags,
        property = mediaDataSettings.showLessImportantTags,
    )

    protected val showSpoilerTagsSection = SortFilterSectionState.SwitchBySetting(
        title = Res.string.anime_media_filter_show_spoiler_tags,
        property = mediaDataSettings.showSpoilerTags,
    )

    // TODO: Actually de-dupe advanced section across controllers
    protected val advancedSection = makeAdvancedSection(
        showLessImportantTagsSection,
        showSpoilerTagsSection,
    )

    open val sections = aniListApi.authedUser
        .mapState(viewModelScope) { viewer ->
            listOfNotNull(
                sortSection,
                statusSection,
                genreSection,
                tagSection,
                myListStatusSection.takeIf { viewer != null },
                averageScoreSection,
                sourceSection,
                licensedBySection,
                titleLanguageSection,
                advancedSection,
            )
        }

    val tagNameInAndNotIn =
        combineStates(mediaTagsController.tags, tagIdIn, tagIdNotIn) { tags, tagIdIn, tagIdNotIn ->
            tagIdIn.mapNotNull { tagId ->
                tags.values.asSequence()
                    .mapNotNull { it.findTag(tagId)?.name }
                    .firstOrNull()
            } to tagIdNotIn.mapNotNull { tagId ->
                tags.values.asSequence()
                    .mapNotNull { it.findTag(tagId)?.name }
                    .firstOrNull()
            }
        }

    @Suppress("UNCHECKED_CAST")
    val mediaFilterParams =
        combineStates(
            sortOption,
            sortAscending,
            statusIn,
            statusNotIn,
            myListStatusIn,
            myListStatusNotIn,
            genreIn,
            genreNotIn,
            tagNameInAndNotIn,
            tagRank,
            averageScore,
            sourceIn,
            licensedByIn,
        ) {
            val myListStatusIn = (it[4] as Set<MediaListStatus?>)
            val myListStatusNotIn = (it[5] as Set<MediaListStatus?>)
            val (tagNameIn, tagNameNotIn) = it[8] as Pair<List<String>, List<String>>
            MediaSearchFilterParams(
                sort = listOf(it[0] as SortType),
                sortAscending = it[1] as Boolean,
                // TODO: Make sets
                statusIn = (it[2] as Set<MediaStatus>).toList(),
                statusNotIn = (it[3] as Set<MediaStatus>).toList(),
                myListStatusIn = myListStatusIn.toList().filterNotNull(),
                myListStatusNotIn = myListStatusNotIn.toList().filterNotNull(),
                onList = when {
                    // Any specific my list status requires onList as API doesn't have a specific
                    // field, so it's manually filtered to inside filterMedia
                    myListStatusIn.isNotEmpty() -> true
                    myListStatusNotIn.contains(null) -> false
                    else -> null
                },
                genreIn = (it[6] as Set<String>).toList(),
                genreNotIn = (it[7] as Set<String>).toList(),
                tagNameIn = tagNameIn,
                tagNameNotIn = tagNameNotIn,
                tagRank = (it[9] as String).toIntOrNull()?.coerceIn(0, 100)
                    ?.takeIf { tagNameIn.isNotEmpty() || tagNameNotIn.isNotEmpty() },
                averageScoreRange = it[10] as RangeData,
                sourceIn = (it[11] as Set<MediaSource>).toList(),
                licensedByIdIn = (it[12] as Set<LicensorsQuery.Data.ExternalLinkSourceCollection>)
                    .mapNotNull { it.siteId },
            )
        }

    open val filterParams = mediaFilterParams

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <Entry : Any> filterMedia(
        result: PagingData<Entry>,
        transform: (Entry) -> MediaPreview,
    ) = combine(
        myListStatusIn.map { it.filterNotNull() },
        myListStatusNotIn.map { it.filterNotNull() },
        mediaTagsController.tags,
        tagIdIn,
        tagShowWhenSpoiler,
    ) { myListStatusIn, myListStatusNotIn, tags, tagIdIn, tagShowWhenSpoiler ->
        result.filterOnIO {
            val media = transform(it)
            val listStatus = media.mediaListEntry?.status
            if (myListStatusNotIn.isNotEmpty() && myListStatusNotIn.contains(listStatus)) {
                return@filterOnIO false
            }

            if (myListStatusIn.isNotEmpty() && !myListStatusIn.contains(listStatus)) {
                return@filterOnIO false
            }

            if (!tagShowWhenSpoiler && tagIdIn.isNotEmpty()) {
                return@filterOnIO tagIdIn.all { tagId ->
                    media.tags?.find { it?.id.toString() == tagId }
                        ?.isMediaSpoiler != true
                }
            }

            true
        }
    }

    val collapseOnClose = mediaDataSettings.collapseAnimeFiltersOnClose

    // Subclasses must provide this as a lazy so that it references the overridden sections
    abstract val state: SortFilterState<MediaSearchFilterParams<SortType>>

    data class InitialParams<SortType : SortOption>(
        val mediaType: MediaType,
        val sortClass: KClass<SortType>,
        val defaultSort: SortType,
        val tagId: String? = null,
        val genre: String? = null,
        val year: Int? = null,
        val mediaListStatus: MediaListStatus? = null,
        val airingDateEnabled: Boolean = year == null,
        val onListEnabled: Boolean = true,
        val showHideIgnored: Boolean = true,
    )
}
