package com.thekeeperofpie.artistalleydatabase.anime.staff.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import artistalleydatabase.modules.anime.staff.generated.resources.Res
import artistalleydatabase.modules.anime.staff.generated.resources.anime_staff_error_loading
import com.anilist.data.fragment.MediaWithListStatus
import com.anilist.data.fragment.StaffDetailsCharacterMediaPage
import com.anilist.data.fragment.StaffDetailsStaffMediaPage
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.characters.data.CharacterDetails
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorites.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffDestinations
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffSettings
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.foldPreviousResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class StaffDetailsViewModel<MediaEntry>(
    private val aniListApi: AuthedAniListApi,
    private val settings: StaffSettings,
    favoritesController: FavoritesController,
    private val mediaListStatusController: MediaListStatusController,
    val ignoreController: IgnoreController,
    private val markdown: Markdown,
    navigationTypeMap: NavigationTypeMap,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
) : ViewModel() {
    private val destination =
        savedStateHandle.toDestination<StaffDestinations.StaffDetails>(navigationTypeMap)
    val staffId = destination.staffId

    val viewer = aniListApi.authedUser

    private val refresh = RefreshFlow()
    var entry = flowForRefreshableContent(refresh, Res.string.anime_staff_error_loading) {
        flowFromSuspend {
            withContext(CustomDispatchers.IO) {
                val staff = aniListApi.staffDetails(staffId)
                val description = staff.description?.let(markdown::convertMarkdownText)
                StaffDetailsScreen.Entry(staff, description)
            }
        }
    }.foldPreviousResult(LoadingResult.loading())
        .stateIn(viewModelScope, SharingStarted.Eagerly, LoadingResult.loading())

    val characters = MutableStateFlow(PagingData.empty<CharacterDetails>())

    val mediaTimeline = MutableStateFlow(StaffMediaTimeline())
    private val mediaTimelineLastRequestedYear = MutableStateFlow<Int?>(null)
    private var mediaTimelineResults = MutableStateFlow(emptyList<StaffDetailsCharacterMediaPage>())

    val staffTimeline = MutableStateFlow(StaffTimeline<MediaEntry>())
    private val staffTimelineLastRequestedYear = MutableStateFlow<Int?>(null)
    private val staffTimelineResults = MutableStateFlow(emptyList<StaffDetailsStaffMediaPage>())
    private val mediaListStatusUpdates =
        MutableStateFlow(emptyMap<String, MediaListStatusController.Update>())

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            entry.mapNotNull { it.result }
                .flatMapLatest { entry ->
                    AniListPager {
                        if (it == 1) {
                            entry.staff.characters.let {
                                it?.pageInfo to it?.nodes?.filterNotNull().orEmpty()
                            }
                        } else {
                            val result = aniListApi.staffDetailsCharactersPage(
                                staffId = entry.staff.id.toString(),
                                page = it,
                            )
                            result.pageInfo to result.nodes.filterNotNull()
                        }
                    }
                }
                .mapLatest {
                    it.mapOnIO {
                        CharacterDetails(
                            id = it.id.toString(),
                            name = it.name,
                            image = it.image?.large,
                            character = it,
                        )
                    }
                }
                .enforceUniqueIds { it.id }
                .cachedIn(viewModelScope)
                .collectLatest(characters::emit)
        }

        // TODO: More robust pagination
        // TODO: Handle ignored
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                mediaTimeline,
                mediaTimelineLastRequestedYear,
                mediaTimelineResults,
                settings.showAdult,
                ::MediaTimelineRefreshParams
            )
                .filter { (timeline) -> timeline.loadMoreState == StaffMediaTimeline.LoadMoreState.None }
                .filter { (_, _, existingResults) ->
                    existingResults.isEmpty() || existingResults.last().pageInfo?.hasNextPage == true
                }
                .filter { (timeline, requestedYear) ->
                    val yearsToCharacters = timeline.yearsToCharacters
                    yearsToCharacters.indexOfFirst { it.first == requestedYear } ==
                            yearsToCharacters.lastIndex
                }
                .onEach { (timeline) ->
                    withContext(CustomDispatchers.Main) {
                        mediaTimeline.value = timeline.copy(
                            loadMoreState = StaffMediaTimeline.LoadMoreState.Loading
                        )
                    }
                }
                .mapLatest { (timeline, _, existingResults, showAdult) ->
                    val nextPage = existingResults.size + 1
                    try {
                        val result =
                            aniListApi.staffDetailsCharacterMediaPagination(staffId, nextPage)
                        val newResults = existingResults + result
                        timeline.copy(
                            yearsToCharacters = calculateMediaTimeline(showAdult, newResults),
                            loadMoreState = StaffMediaTimeline.LoadMoreState.None
                        ) to newResults
                    } catch (throwable: Throwable) {
                        timeline.copy(
                            loadMoreState = StaffMediaTimeline.LoadMoreState.Error(
                                throwable
                            )
                        ) to existingResults
                    }
                }
                .collectLatest { (timeline, newResults) ->
                    mediaTimelineResults.value = newResults
                    withContext(CustomDispatchers.Main) {
                        mediaTimeline.value = timeline
                    }
                }
        }

        // Each staff timeline emission will reset the Flow, so the default behavior of allChanges()
        // won't work as it'll lose all previous changes made before the current page. Instead,
        // manually cache every update made since the screen was shown.
        viewModelScope.launch(CustomDispatchers.IO) {
            mediaListStatusController.allChanges()
                .collectLatest(mediaListStatusUpdates::emit)
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                staffTimeline,
                staffTimelineLastRequestedYear,
                staffTimelineResults,
                settings.showAdult,
                ::StaffTimelineRefreshParams
            )
                .filter { (timeline) -> timeline.loadMoreState == StaffTimeline.LoadMoreState.None }
                .filter { (_, _, existingResults) ->
                    existingResults.isEmpty() || existingResults.last().pageInfo?.hasNextPage == true
                }
                .filter { (timeline, requestedYear) ->
                    val yearsToMedia = timeline.yearsToMedia
                    yearsToMedia.indexOfFirst { it.first == requestedYear } ==
                            yearsToMedia.lastIndex
                }
                .onEach { (timeline) ->
                    withContext(CustomDispatchers.Main) {
                        staffTimeline.value = timeline.copy(
                            loadMoreState = StaffTimeline.LoadMoreState.Loading
                        )
                    }
                }
                .mapLatest { (timeline, _, existingResults, showAdult) ->
                    val nextPage = existingResults.size + 1
                    try {
                        val result =
                            aniListApi.staffDetailsStaffMediaPagination(staffId, nextPage)
                        val newResults = existingResults + result
                        timeline.copy(
                            yearsToMedia = calculateStaffTimeline(showAdult, newResults),
                            loadMoreState = StaffTimeline.LoadMoreState.None
                        ) to newResults
                    } catch (throwable: Throwable) {
                        timeline.copy(
                            loadMoreState = StaffTimeline.LoadMoreState.Error(
                                throwable
                            )
                        ) to existingResults
                    }
                }
                .flatMapLatest { (timeline, newResults) ->
                    combine(
                        mediaListStatusUpdates,
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { updates, _, filteringData ->
                        timeline.copy(
                            yearsToMedia = timeline.yearsToMedia.map { (year, media) ->
                                year to media.mapNotNull {
                                    applyMediaFiltering(
                                        statuses = updates,
                                        ignoreController = ignoreController,
                                        filteringData = filteringData,
                                        entry = it,
                                        filterableData = mediaEntryProvider.mediaFilterable(it.mediaEntry),
                                        copy = {
                                            copy(
                                                mediaEntry = mediaEntryProvider
                                                    .copyMediaEntry(mediaEntry, it)
                                            )
                                        },
                                    )
                                }
                            }
                        ) to newResults
                    }
                }
                .collectLatest { (timeline, newResults) ->
                    staffTimelineResults.value = newResults
                    withContext(CustomDispatchers.Main) {
                        staffTimeline.value = timeline
                    }
                }
        }

        favoritesToggleHelper.initializeTracking(
            scope = viewModelScope,
            entry = { entry.map { it.result } },
            entryToId = { it.staff.id.toString() },
            entryToType = { FavoriteType.STAFF },
            entryToFavorite = { it.staff.isFavourite },
        )
    }

    fun refresh() = refresh.refresh(fromUser = true)

    fun onRequestMediaYear(year: Int?) {
        val existingValue = mediaTimelineLastRequestedYear.value
        if (existingValue == null || (year != null && year < existingValue)) {
            mediaTimelineLastRequestedYear.value = year
        }
    }

    fun onRequestStaffYear(year: Int?) {
        val existingValue = staffTimelineLastRequestedYear.value
        if (existingValue == null || (year != null && year < existingValue)) {
            staffTimelineLastRequestedYear.value = year
        }
    }

    private fun calculateMediaTimeline(
        showAdult: Boolean,
        results: List<StaffDetailsCharacterMediaPage>,
    ) = results.flatMap { it.edges?.filterNotNull().orEmpty() }
        .groupBy { it.node?.startDate?.year }
        .mapValues {
            it.value.sortedWith(
                // TODO: Offer option to sort by favorites instead
                compareBy<StaffDetailsCharacterMediaPage.Edge, Int?>(nullsLast()) {
                    it.node?.startDate?.year
                }
                    .thenBy(nullsLast()) { it.node?.startDate?.month }
                    .thenBy(nullsLast()) { it.node?.startDate?.day }
                    .reversed()
            )
                .filter { if (showAdult) true else it.node?.isAdult == false }
                .flatMap { edge ->
                    edge.characters?.filterNotNull().orEmpty()
                        .map {
                            StaffMediaTimeline.Character(
                                id = "${edge.node?.id}_${it.id}",
                                character = it,
                                role = edge.characterRole,
                                media = edge.node,
                            )
                        }
                }
                .distinctBy { it.id }
        }
        .entries
        .sortedWith(compareByDescending(nullsLast()) { it.key })
        .map { it.toPair() }

    private fun calculateStaffTimeline(
        showAdult: Boolean,
        results: List<StaffDetailsStaffMediaPage>,
    ) = results.flatMap { it.edges?.filterNotNull().orEmpty() }
        .groupBy { it.node?.startDate?.year }
        .mapValues {
            it.value.sortedWith(
                compareBy<StaffDetailsStaffMediaPage.Edge, Int?>(nullsLast()) {
                    it.node?.startDate?.year
                }
                    .thenBy(nullsLast()) { it.node?.startDate?.month }
                    .thenBy(nullsLast()) { it.node?.startDate?.day }
                    .reversed()
            )
                .filter { if (showAdult) true else it.node?.isAdult == false }
                .mapNotNull { edge ->
                    val node = edge.node ?: return@mapNotNull null
                    StaffTimeline.MediaWithRole<MediaEntry>(
                        id = "${edge.id}_${node.id}",
                        mediaEntry = mediaEntryProvider.mediaEntry(node),
                        role = edge.staffRole,
                    )
                }
                .distinctBy { it.id }
        }
        .entries
        .sortedWith(compareByDescending(nullsLast()) { it.key })
        .map { it.toPair() }

    data class MediaTimelineRefreshParams(
        val timeline: StaffMediaTimeline,
        val requestedYear: Int?,
        val existingResults: List<StaffDetailsCharacterMediaPage>,
        val showAdult: Boolean,
    )

    data class StaffTimelineRefreshParams<MediaEntry>(
        val timeline: StaffTimeline<MediaEntry>,
        val requestedYear: Int?,
        val existingResults: List<StaffDetailsStaffMediaPage>,
        val showAdult: Boolean,
    )

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val settings: StaffSettings,
        private val favoritesController: FavoritesController,
        private val mediaListStatusController: MediaListStatusController,
        private val ignoreController: IgnoreController,
        private val markdown: Markdown,
        private val navigationTypeMap: NavigationTypeMap,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {
        fun <MediaEntry> create(
            mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
        ) = StaffDetailsViewModel(
            aniListApi = aniListApi,
            settings = settings,
            favoritesController = favoritesController,
            mediaListStatusController = mediaListStatusController,
            ignoreController = ignoreController,
            markdown = markdown,
            navigationTypeMap = navigationTypeMap,
            savedStateHandle = savedStateHandle,
            mediaEntryProvider = mediaEntryProvider,
        )
    }
}
