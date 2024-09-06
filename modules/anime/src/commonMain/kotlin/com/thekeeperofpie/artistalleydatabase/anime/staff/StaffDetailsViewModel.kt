package com.thekeeperofpie.artistalleydatabase.anime.staff

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.fragment.StaffDetailsCharacterMediaPage
import com.anilist.fragment.StaffDetailsStaffMediaPage
import com.anilist.type.CharacterRole
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_staff_error_loading
import com.thekeeperofpie.artistalleydatabase.anime.character.DetailsCharacter
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoriteType
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesController
import com.thekeeperofpie.artistalleydatabase.anime.favorite.FavoritesToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.utils_compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StaffDetailsViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    favoritesController: FavoritesController,
    private val mediaListStatusController: MediaListStatusController,
    val ignoreController: IgnoreController,
    private val markdown: Markdown,
    savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
) : ViewModel() {
    private val destination = savedStateHandle.toDestination<AnimeDestination.StaffDetails>(navigationTypeMap)
    val staffId = destination.staffId

    val viewer = aniListApi.authedUser

    var entry by mutableStateOf<StaffDetailsScreen.Entry?>(null)
    var loading by mutableStateOf(true)
    var error by mutableStateOf<Pair<StringResource, Exception?>?>(null)
    val showAdult get() = settings.showAdult

    val characters = MutableStateFlow(PagingData.empty<DetailsCharacter>())

    val mediaTimeline = MutableStateFlow(MediaTimeline())
    private val mediaTimelineLastRequestedYear = MutableStateFlow<Int?>(null)
    private var mediaTimelineResults = MutableStateFlow(emptyList<StaffDetailsCharacterMediaPage>())

    val staffTimeline = MutableStateFlow(StaffTimeline())
    private val staffTimelineLastRequestedYear = MutableStateFlow<Int?>(null)
    private val staffTimelineResults = MutableStateFlow(emptyList<StaffDetailsStaffMediaPage>())
    private val mediaListStatusUpdates =
        MutableStateFlow(emptyMap<String, MediaListStatusController.Update>())

    val favoritesToggleHelper =
        FavoritesToggleHelper(aniListApi, favoritesController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                val staff = aniListApi.staffDetails(staffId)
                val description = staff.description?.let(markdown::convertMarkdownText)
                val entry = StaffDetailsScreen.Entry(staff, description)
                withContext(CustomDispatchers.Main) {
                    this@StaffDetailsViewModel.entry = entry
                }
            } catch (exception: Exception) {
                withContext(CustomDispatchers.Main) {
                    error = Res.string.anime_staff_error_loading to exception
                }
            } finally {
                withContext(CustomDispatchers.Main) {
                    loading = false
                }
            }
        }

        viewModelScope.launch(CustomDispatchers.IO) {
            snapshotFlow { entry }
                .filterNotNull()
                .flowOn(CustomDispatchers.Main)
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
                        DetailsCharacter(
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
                .filter { (timeline) -> timeline.loadMoreState == MediaTimeline.LoadMoreState.None }
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
                            loadMoreState = MediaTimeline.LoadMoreState.Loading
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
                            loadMoreState = MediaTimeline.LoadMoreState.None
                        ) to newResults
                    } catch (throwable: Throwable) {
                        timeline.copy(
                            loadMoreState = MediaTimeline.LoadMoreState.Error(
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
                        settings.showIgnored,
                        settings.showAdult,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { updates, _, showIgnored, showAdult, showLessImportantTags, showSpoilerTags ->
                        timeline.copy(
                            yearsToMedia = timeline.yearsToMedia.map { (year, media) ->
                                year to media.mapNotNull {
                                    applyMediaFiltering(
                                        statuses = updates,
                                        ignoreController = ignoreController,
                                        showAdult = showAdult,
                                        showIgnored = showIgnored,
                                        showLessImportantTags = showLessImportantTags,
                                        showSpoilerTags = showSpoilerTags,
                                        entry = it,
                                        transform = { it },
                                        media = it.media,
                                        copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                            copy(
                                                mediaListStatus = mediaListStatus,
                                                progress = progress,
                                                progressVolumes = progressVolumes,
                                                scoreRaw = scoreRaw,
                                                ignored = ignored,
                                                showLessImportantTags = showLessImportantTags,
                                                showSpoilerTags = showSpoilerTags,
                                            )
                                        }
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
            entry = { snapshotFlow { entry } },
            entryToId = { it.staff.id.toString() },
            entryToType = { FavoriteType.STAFF },
            entryToFavorite = { it.staff.isFavourite },
        )
    }

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
                            MediaTimeline.Character(
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
                    StaffTimeline.Media(
                        id = "${edge.id}_${node.id}",
                        media = node,
                        role = edge.staffRole,
                    )
                }
                .distinctBy { it.id }
        }
        .entries
        .sortedWith(compareByDescending(nullsLast()) { it.key })
        .map { it.toPair() }

    data class MediaTimelineRefreshParams(
        val timeline: MediaTimeline,
        val requestedYear: Int?,
        val existingResults: List<StaffDetailsCharacterMediaPage>,
        val showAdult: Boolean,
    )

    data class MediaTimeline(
        val yearsToCharacters: List<Pair<Int?, List<Character>>> = emptyList(),
        val loadMoreState: LoadMoreState = LoadMoreState.None,
    ) {
        data class Character(
            val id: String,
            val character: StaffDetailsCharacterMediaPage.Edge.Character,
            val role: CharacterRole?,
            val media: StaffDetailsCharacterMediaPage.Edge.Node?,
        )

        sealed interface LoadMoreState {
            data object None : LoadMoreState
            data object Loading : LoadMoreState
            data class Error(val throwable: Throwable) : LoadMoreState
        }
    }

    data class StaffTimelineRefreshParams(
        val timeline: StaffTimeline,
        val requestedYear: Int?,
        val existingResults: List<StaffDetailsStaffMediaPage>,
        val showAdult: Boolean,
    )

    data class StaffTimeline(
        val yearsToMedia: List<Pair<Int?, List<Media>>> = emptyList(),
        val loadMoreState: LoadMoreState = LoadMoreState.None,
    ) {
        data class Media(
            val id: String,
            val role: String?,
            override val media: StaffDetailsStaffMediaPage.Edge.Node,
            override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
            override val progress: Int? = media.mediaListEntry?.progress,
            override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
            override val scoreRaw: Double? = media.mediaListEntry?.score,
            override val ignored: Boolean = false,
            override val showLessImportantTags: Boolean = false,
            override val showSpoilerTags: Boolean = false,
            override val type: MediaType? = media.type,
            override val color: Color? = media.coverImage?.color
                ?.let(ComposeColorUtils::hexToColor),
            override val maxProgress: Int? = MediaUtils.maxProgress(
                type = media.type,
                chapters = media.chapters,
                episodes = media.episodes,
                nextAiringEpisode = media.nextAiringEpisode?.episode,
            ),
            override val maxProgressVolumes: Int? = media.volumes,
            override val averageScore: Int? = media.averageScore,
        ) : MediaGridCard.Entry

        sealed interface LoadMoreState {
            data object None : LoadMoreState
            data object Loading : LoadMoreState
            data class Error(val throwable: Throwable) : LoadMoreState
        }
    }
}
