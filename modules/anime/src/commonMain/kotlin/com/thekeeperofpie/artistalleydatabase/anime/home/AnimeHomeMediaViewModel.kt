package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_home_anime_current_header
import artistalleydatabase.modules.anime.generated.resources.anime_home_error_loading_anime
import artistalleydatabase.modules.anime.generated.resources.anime_home_error_loading_manga
import artistalleydatabase.modules.anime.generated.resources.anime_home_last_added
import artistalleydatabase.modules.anime.generated.resources.anime_home_manga_current_header
import artistalleydatabase.modules.anime.generated.resources.anime_home_popular_last_season
import artistalleydatabase.modules.anime.generated.resources.anime_home_popular_next_season
import artistalleydatabase.modules.anime.generated.resources.anime_home_popular_this_season
import artistalleydatabase.modules.anime.generated.resources.anime_home_suggestion_popular_all_time
import artistalleydatabase.modules.anime.generated.resources.anime_home_suggestion_top
import artistalleydatabase.modules.anime.generated.resources.anime_home_top_released_this_year
import artistalleydatabase.modules.anime.generated.resources.anime_home_trending_row_label
import com.anilist.fragment.HomeMedia
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.media.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewEntry
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapNotNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AnimeHomeMediaViewModel(
    protected val aniListApi: AuthedAniListApi,
    protected val settings: AnimeSettings,
    private val ignoreController: IgnoreController,
    protected val userMediaListController: UserMediaListController,
    private val mediaListStatusController: MediaListStatusController,
    val currentHeaderTextRes: StringResource,
    val mediaType: MediaType,
    private val errorTextRes: StringResource,
    // Track the previous size to inform placeholder count; this probably isn't worth it
    val currentMediaPreviousSize: MutableStateFlow<Int>,
) : ViewModel() {

    var entry by mutableStateOf<LoadingResult<AnimeHomeDataEntry>>(LoadingResult.loading())
    var currentMedia by mutableStateOf(
        LoadingResult.loading<List<UserMediaListController.MediaEntry>>()
            .copy(result = if (currentMediaPreviousSize.value == 0) emptyList() else null)
    )
    val reviews = MutableStateFlow(PagingData.empty<ReviewEntry>())

    private val refresh = RefreshFlow()

    init {
        collectCurrent()
        collectMedia()
        collectReviews()
    }

    protected abstract suspend fun rows(): Flow<List<RowInput>>

    abstract val suggestions: List<Pair<StringResource, AnimeDestination>>

    // It is faster to load the specific current list,
    // but this duplicates with the full user media lists
    // TODO: Make this more efficient when user list caching is available
    protected suspend fun current() = aniListApi.authedUser.flatMapLatest {
        if (it == null) {
            flowOf(LoadingResult.success(emptyList()))
        } else {
            aniListApi.viewerMediaList(
                userId = it.id,
                type = mediaType,
                status = MediaListStatus.CURRENT,
                includeDescription = false,
            ).map {
                it.transformResult {
                    it.lists
                        ?.find { it?.status == MediaListStatus.CURRENT }
                        ?.let(UserMediaListController::ListEntry)
                        ?.entries
                        .orEmpty()
                }
            }
        }
    }

    @Composable
    fun currentMediaState(): CurrentMediaState {
        val previousSize by currentMediaPreviousSize.collectAsState()
        return remember(currentMedia, previousSize) {
            CurrentMediaState(
                result = currentMedia,
                headerTextRes = currentHeaderTextRes,
                mediaType = mediaType,
                previousSize = previousSize,
            )
        }
    }

    fun refresh() {
        refresh.refresh()
        userMediaListController.refresh(mediaType)
    }

    private fun collectCurrent() {
        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.updates
                .flatMapLatest { current() }
                .flatMapLatest { current ->
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { mediaStatusUpdates, _, filteringData ->
                        current.transformResult {
                            it.mapNotNull {
                                applyMediaFiltering(
                                    statuses = mediaStatusUpdates,
                                    ignoreController = ignoreController,
                                    filteringData = filteringData,
                                    entry = it,
                                    filterableData = it.mediaFilterable,
                                    copy = { copy(mediaFilterable = it) },
                                )
                            }
                        }
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest {
                    currentMedia = it
                    val result = it.result
                    if (it.success && result != null) {
                        currentMediaPreviousSize.emit(result.size)
                    }
                }
        }
    }

    private fun collectMedia() {
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(refresh.updates, errorTextRes) { rows() }
                .flatMapLatest { mediaResult ->
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { mediaStatusUpdates, _, filteringData ->
                        mediaResult.transformResult { rows ->
                            AnimeHomeDataEntry(
                                lists = rows.map {
                                    AnimeHomeDataEntry.RowData(
                                        id = it.id,
                                        titleRes = it.titleRes,
                                        entries = it.list?.filterNotNull()
                                            ?.map(AnimeHomeDataEntry::MediaEntry)
                                            ?.mapNotNull {
                                                applyMediaFiltering(
                                                    statuses = mediaStatusUpdates,
                                                    ignoreController = ignoreController,
                                                    filteringData = filteringData,
                                                    entry = it,
                                                    filterableData = it.mediaFilterable,
                                                    copy = { copy(mediaFilterable = it) },
                                                )
                                            },
                                        viewAllRoute = it.viewAllRoute,
                                    )
                                },
                            )
                        }
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }
    }

    private fun collectReviews() {
        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.updates
                .flatMapLatest {
                AniListPager(perPage = 6, prefetchDistance = 1) {
                    val result =
                        aniListApi.homeReviews(mediaType = mediaType, page = it, perPage = 6)
                    result.page.pageInfo to result.page.reviews.filterNotNull()
                }
            }
                .mapLatest {
                    it.mapOnIO {
                        ReviewEntry(it, MediaCompactWithTagsEntry(it.media))
                    }
                }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.mediaFilteringData(),
                    ) { mediaStatusUpdates, _, filteringData ->
                        pagingData
                            .mapNotNull {
                                applyMediaFiltering(
                                    statuses = mediaStatusUpdates,
                                    ignoreController = ignoreController,
                                    filteringData = filteringData,
                                    entry = it,
                                    filterableData = it.media.mediaFilterable,
                                    copy = { copy(media = media.copy(mediaFilterable = it)) },
                                )
                            }
                    }
                }
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(reviews::emit)
        }
    }

    @Inject
    class Anime(
        aniListApi: AuthedAniListApi,
        settings: AnimeSettings,
        ignoreController: IgnoreController,
        userMediaListController: UserMediaListController,
        mediaListStatusController: MediaListStatusController,
    ) : AnimeHomeMediaViewModel(
        aniListApi = aniListApi,
        settings = settings,
        ignoreController = ignoreController,
        userMediaListController = userMediaListController,
        mediaListStatusController = mediaListStatusController,
        currentHeaderTextRes = Res.string.anime_home_anime_current_header,
        mediaType = MediaType.ANIME,
        errorTextRes = Res.string.anime_home_error_loading_anime,
        currentMediaPreviousSize = settings.currentMediaListSizeAnime,
    ) {
        override val suggestions = listOf(
            Res.string.anime_home_suggestion_popular_all_time to AnimeDestination.SearchMedia(
                title = AnimeDestination.SearchMedia.Title.HomeSuggestionPopularAllTime,
                mediaType = mediaType,
                sort = MediaSortOption.POPULARITY
            ),
            Res.string.anime_home_suggestion_top to AnimeDestination.SearchMedia(
                title = AnimeDestination.SearchMedia.Title.HomeSuggestionTop,
                mediaType = mediaType,
                sort = MediaSortOption.SCORE,
            ),
        )

        override suspend fun rows() = flow {
            val trending = RowInput(
                id = "anime_trending",
                titleRes = Res.string.anime_home_trending_row_label,
                viewAllRoute = AnimeDestination.SearchMedia(
                    title = AnimeDestination.SearchMedia.Title.HomeTrending,
                    mediaType = mediaType,
                    sort = MediaSortOption.TRENDING,
                )
            )
            val popularThisSeason = RowInput(
                id = "anime_popular_this_season",
                titleRes = Res.string.anime_home_popular_this_season,
                viewAllRoute = AnimeDestination.Seasonal(AnimeDestination.Seasonal.Type.THIS)
            )
            val lastAdded = RowInput(
                id = "anime_last_added",
                titleRes = Res.string.anime_home_last_added,
                viewAllRoute = AnimeDestination.SearchMedia(
                    title = AnimeDestination.SearchMedia.Title.HomeLastAdded,
                    mediaType = mediaType,
                    sort = MediaSortOption.ID,
                )
            )
            val popularLastSeason = RowInput(
                id = "anime_popular_last_season",
                titleRes = Res.string.anime_home_popular_last_season,
                viewAllRoute = AnimeDestination.Seasonal(AnimeDestination.Seasonal.Type.LAST)
            )
            val popularNextSeason = RowInput(
                id = "anime_popular_next_season",
                titleRes = Res.string.anime_home_popular_next_season,
                viewAllRoute = AnimeDestination.Seasonal(AnimeDestination.Seasonal.Type.NEXT)
            )

            emit(
                listOf(
                    trending,
                    popularThisSeason,
                    lastAdded,
                    popularLastSeason,
                    popularNextSeason,
                )
            )

            val result = aniListApi.homeAnime()
            emit(
                listOf(
                    trending.copy(list = result.trending?.media.orEmpty()),
                    popularThisSeason.copy(list = result.popularThisSeason?.media.orEmpty()),
                    lastAdded.copy(list = result.lastAdded?.media.orEmpty()),
                    popularLastSeason.copy(list = result.popularLastSeason?.media.orEmpty()),
                    popularNextSeason.copy(list = result.popularNextSeason?.media.orEmpty()),
                )
            )
        }
    }

    @Inject
    class Manga(
        aniListApi: AuthedAniListApi,
        settings: AnimeSettings,
        ignoreController: IgnoreController,
        userMediaListController: UserMediaListController,
        mediaListStatusController: MediaListStatusController,
    ) : AnimeHomeMediaViewModel(
        aniListApi = aniListApi,
        settings = settings,
        ignoreController = ignoreController,
        userMediaListController = userMediaListController,
        mediaListStatusController = mediaListStatusController,
        currentHeaderTextRes = Res.string.anime_home_manga_current_header,
        mediaType = MediaType.MANGA,
        errorTextRes = Res.string.anime_home_error_loading_manga,
        currentMediaPreviousSize = settings.currentMediaListSizeManga,
    ) {
        override val suggestions = listOf(
            Res.string.anime_home_suggestion_popular_all_time to AnimeDestination.SearchMedia(
                title = AnimeDestination.SearchMedia.Title.HomeSuggestionPopularAllTime,
                mediaType = mediaType,
                sort = MediaSortOption.POPULARITY,
            ),
            Res.string.anime_home_suggestion_top to AnimeDestination.SearchMedia(
                title = AnimeDestination.SearchMedia.Title.HomeSuggestionTop,
                mediaType = mediaType,
                sort = MediaSortOption.SCORE,
            ),
        )

        override suspend fun rows() = flow {
            val trending = RowInput(
                id = "manga_trending",
                titleRes = Res.string.anime_home_trending_row_label,
                viewAllRoute = AnimeDestination.SearchMedia(
                    title = AnimeDestination.SearchMedia.Title.HomeTrending,
                    mediaType = mediaType,
                    sort = MediaSortOption.TRENDING,
                )
            )
            val lastAdded = RowInput(
                id = "manga_last_added",
                titleRes = Res.string.anime_home_last_added,
                viewAllRoute = AnimeDestination.SearchMedia(
                    title = AnimeDestination.SearchMedia.Title.HomeLastAdded,
                    mediaType = mediaType,
                    sort = MediaSortOption.ID,
                )
            )
            val topReleasedThisYear = RowInput(
                id = "manga_top_released_this_year",
                titleRes = Res.string.anime_home_top_released_this_year,
                viewAllRoute = AnimeDestination.SearchMedia(
                    title = AnimeDestination.SearchMedia.Title.HomeReleasedThisYear,
                    mediaType = mediaType,
                    sort = MediaSortOption.SCORE,
                )
            )
            emit(
                listOf(
                    trending,
                    lastAdded,
                    topReleasedThisYear,
                )
            )

            val result = aniListApi.homeManga()
            emit(
                listOf(
                    trending.copy(list = result.trending?.media.orEmpty()),
                    lastAdded.copy(list = result.lastAdded?.media.orEmpty()),
                    topReleasedThisYear.copy(list = result.topThisYear?.media.orEmpty()),
                )
            )
        }
    }

    data class RowInput(
        val id: String,
        val titleRes: StringResource,
        val viewAllRoute: AnimeDestination,
        val list: List<HomeMedia?>? = null,
    )

    data class CurrentMediaState(
        val result: LoadingResult<List<UserMediaListController.MediaEntry>>,
        val headerTextRes: StringResource,
        val mediaType: MediaType,
        val previousSize: Int,
    )
}
