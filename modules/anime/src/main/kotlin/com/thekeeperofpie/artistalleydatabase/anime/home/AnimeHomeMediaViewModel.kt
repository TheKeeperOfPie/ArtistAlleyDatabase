package com.thekeeperofpie.artistalleydatabase.anime.home

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.fragment.HomeMedia
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.hoc081098.flowext.combine
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.emptyImmutableList
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AnimeHomeMediaViewModel(
    protected val aniListApi: AuthedAniListApi,
    protected val settings: AnimeSettings,
    private val ignoreController: IgnoreController,
    protected val userMediaListController: UserMediaListController,
    private val mediaListStatusController: MediaListStatusController,
    @StringRes val currentHeaderTextRes: Int,
    val mediaType: MediaType,
    @StringRes private val errorTextRes: Int,
    // Track the previous size to inform placeholder count; this probably isn't worth it
    val currentMediaPreviousSize: MutableStateFlow<Int>,
) : ViewModel() {

    var entry by mutableStateOf<LoadingResult<AnimeHomeDataEntry>>(LoadingResult.loading())
    var currentMedia by mutableStateOf<LoadingResult<ImmutableList<UserMediaListController.MediaEntry>>>(
        LoadingResult.loading<ImmutableList<UserMediaListController.MediaEntry>>()
            .copy(result = if (currentMediaPreviousSize.value == 0) emptyImmutableList() else null)
    )
    val reviews = MutableStateFlow(PagingData.empty<ReviewEntry>())

    private val refresh = MutableStateFlow(-1L)

    init {
        collectCurrent()
        collectMedia()
        collectReviews()
    }

    protected abstract suspend fun rows(): Flow<List<RowInput>>

    abstract val suggestions: List<Pair<Int, String>>

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

    fun refresh() {
        val refresh = SystemClock.uptimeMillis()
        userMediaListController.refresh(mediaType)
        this.refresh.value = refresh
    }

    private fun collectCurrent() {
        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.flatMapLatest { current() }
                .flatMapLatest { current ->
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.showAdult,
                        settings.showIgnored,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { mediaStatusUpdates, _, showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
                        current.transformResult {
                            it.mapNotNull {
                                applyMediaFiltering(
                                    statuses = mediaStatusUpdates,
                                    ignoreController = ignoreController,
                                    showAdult = showAdult,
                                    showIgnored = showIgnored,
                                    showLessImportantTags = showLessImportantTags,
                                    showSpoilerTags = showSpoilerTags,
                                    entry = it,
                                    transform = { it },
                                    media = it.media,
                                    copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                        UserMediaListController.MediaEntry(
                                            media = media,
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
                            }.toImmutableList()
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
            flowForRefreshableContent(refresh, errorTextRes) { rows() }
                .flatMapLatest { mediaResult ->
                    combine(
                        mediaListStatusController.allChanges(),
                        ignoreController.updates(),
                        settings.showAdult,
                        settings.showIgnored,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { mediaStatusUpdates, _, showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
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
                                                    showAdult = showAdult,
                                                    showIgnored = showIgnored,
                                                    showLessImportantTags = showLessImportantTags,
                                                    showSpoilerTags = showSpoilerTags,
                                                    entry = it,
                                                    transform = { it },
                                                    mediaId = it.media.id.toString(),
                                                    isAdult = it.media.isAdult,
                                                    status = it.media.mediaListEntry?.status,
                                                    progress = it.media.mediaListEntry?.progress,
                                                    progressVolumes = it.media.mediaListEntry?.progressVolumes,
                                                    scoreRaw = it.media.mediaListEntry?.score,
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
            refresh.flatMapLatest {
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
                        settings.showAdult,
                        settings.showIgnored,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { mediaStatusUpdates, _, showAdult, showIgnored, showLessImportantTags, showSpoilerTags ->
                        pagingData.mapNotNull {
                            applyMediaFiltering(
                                statuses = mediaStatusUpdates,
                                ignoreController = ignoreController,
                                showAdult = showAdult,
                                showIgnored = showIgnored,
                                showLessImportantTags = showLessImportantTags,
                                showSpoilerTags = showSpoilerTags,
                                entry = it,
                                transform = { it.media },
                                media = it.media.media,
                                copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                    copy(
                                        media = media.copy(
                                            mediaListStatus = mediaListStatus,
                                            progress = progress,
                                            progressVolumes = progressVolumes,
                                            scoreRaw = scoreRaw,
                                            ignored = ignored,
                                            showLessImportantTags = showLessImportantTags,
                                            showSpoilerTags = showSpoilerTags,
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(reviews::emit)
        }
    }

    @HiltViewModel
    class Anime @Inject constructor(
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
        currentHeaderTextRes = R.string.anime_home_anime_current_header,
        mediaType = MediaType.ANIME,
        errorTextRes = R.string.anime_home_error_loading_anime,
        currentMediaPreviousSize = settings.currentMediaListSizeAnime,
    ) {
        override val suggestions = listOf(
            R.string.anime_home_suggestion_popular_all_time to AnimeNavDestinations.SEARCH_MEDIA.id
                    + "?titleRes=${R.string.anime_home_suggestion_popular_all_time}"
                    + "&mediaType=${mediaType.name}"
                    + "&sort=${MediaSortOption.POPULARITY}",
            R.string.anime_home_suggestion_top to AnimeNavDestinations.SEARCH_MEDIA.id
                    + "?titleRes=${R.string.anime_home_suggestion_top}"
                    + "&mediaType=${mediaType.name}"
                    + "&sort=${MediaSortOption.SCORE}",
        )

        override suspend fun rows() = flow {
            val trending = RowInput(
                id = "anime_trending",
                titleRes = R.string.anime_home_trending_row_label,
                viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                        + "?titleRes=${R.string.anime_home_trending_screen_title}"
                        + "&mediaType=${mediaType.name}"
                        + "&sort=${MediaSortOption.TRENDING}"
            )
            val popularThisSeason = RowInput(
                id = "anime_popular_this_season",
                titleRes = R.string.anime_home_popular_this_season,
                viewAllRoute = "${AnimeNavDestinations.SEASONAL.id}?type=${SeasonalViewModel.Type.THIS.name}"
            )
            val lastAdded = RowInput(
                id = "anime_last_added",
                titleRes = R.string.anime_home_last_added,
                viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                        + "?titleRes=${R.string.anime_home_last_added_screen_title}"
                        + "&mediaType=${mediaType.name}"
                        + "&sort=${MediaSortOption.ID}"
            )
            val popularLastSeason = RowInput(
                id = "anime_popular_last_season",
                titleRes = R.string.anime_home_popular_last_season,
                viewAllRoute = "${AnimeNavDestinations.SEASONAL.id}?type=${SeasonalViewModel.Type.LAST.name}"
            )
            val popularNextSeason = RowInput(
                id = "anime_popular_next_season",
                titleRes = R.string.anime_home_popular_next_season,
                viewAllRoute = "${AnimeNavDestinations.SEASONAL.id}?type=${SeasonalViewModel.Type.NEXT.name}"
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

    @HiltViewModel
    class Manga @Inject constructor(
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
        currentHeaderTextRes = R.string.anime_home_manga_current_header,
        mediaType = MediaType.MANGA,
        errorTextRes = R.string.anime_home_error_loading_manga,
        currentMediaPreviousSize = settings.currentMediaListSizeManga,
    ) {
        override val suggestions = listOf(
            R.string.anime_home_suggestion_popular_all_time to AnimeNavDestinations.SEARCH_MEDIA.id
                    + "?titleRes=${R.string.anime_home_suggestion_popular_all_time}"
                    + "&mediaType=${mediaType.name}"
                    + "&sort=${MediaSortOption.POPULARITY}",
            R.string.anime_home_suggestion_top to AnimeNavDestinations.SEARCH_MEDIA.id
                    + "?titleRes=${R.string.anime_home_suggestion_top}"
                    + "&mediaType=${mediaType.name}"
                    + "&sort=${MediaSortOption.SCORE}",
        )

        override suspend fun rows() = flow {
            val trending = RowInput(
                id = "manga_trending",
                titleRes = R.string.anime_home_trending_row_label,
                viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                        + "?titleRes=${R.string.anime_home_trending_screen_title}"
                        + "&mediaType=${mediaType.name}"
                        + "&sort=${MediaSortOption.TRENDING}"
            )
            val lastAdded = RowInput(
                id = "manga_last_added",
                titleRes = R.string.anime_home_last_added,
                viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                        + "?titleRes=${R.string.anime_home_last_added_screen_title}"
                        + "&mediaType=${mediaType.name}"
                        + "&sort=${MediaSortOption.ID}"
            )
            val topReleasedThisYear = RowInput(
                id = "manga_top_released_this_year",
                titleRes = R.string.anime_home_top_released_this_year,
                viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                        + "?titleRes=${R.string.anime_home_top_released_this_year_title}"
                        + "&mediaType=${mediaType.name}"
                        + "&sort=${MediaSortOption.SCORE}"
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
        val titleRes: Int,
        val viewAllRoute: String,
        val list: List<HomeMedia?>? = null,
    )
}
