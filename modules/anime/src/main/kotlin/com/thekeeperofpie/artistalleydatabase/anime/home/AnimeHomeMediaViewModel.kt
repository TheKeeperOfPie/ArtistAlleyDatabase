package com.thekeeperofpie.artistalleydatabase.anime.home

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.anilist.fragment.MediaAndReviewsReview
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.fragment.UserNavigationData
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.anilist.type.RecommendationRating
import com.hoc081098.flowext.combine
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationData
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationStatusController
import com.thekeeperofpie.artistalleydatabase.anime.recommendation.RecommendationToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.review.ReviewEntry
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.filterOnIO
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapNotNull
import com.thekeeperofpie.artistalleydatabase.anime.utils.mapOnIO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
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
) : ViewModel() {

    var entry by mutableStateOf<LoadingResult<AnimeHomeDataEntry>>(LoadingResult.loading())
    var current by mutableStateOf<LoadingResult<List<UserMediaListController.MediaEntry>>>(
        LoadingResult.loading()
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
    suspend fun current() = aniListApi.authedUser.flatMapLatest {
        if (it == null) {
            flowOf(LoadingResult.empty())
        } else {
            aniListApi.userMediaList(
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
                        current.copy(
                            result = current.result?.mapNotNull {
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
                            },
                            error = current.error?.copy(first = errorTextRes),
                        )
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { current = it }
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
                                            .orEmpty()
                                            .mapNotNull {
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
                Pager(config = PagingConfig(0)) {
                    AniListPagingSource(perPage = 5) {
                        val result =
                            aniListApi.homeReviews(mediaType = mediaType, page = it, perPage = 5)
                        result.page.pageInfo to result.page.reviews.filterNotNull()
                    }
                }.flow
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

        override suspend fun rows() = flowFromSuspend {
            val result = aniListApi.homeAnime()
            listOf(
                RowInput(
                    "anime_trending",
                    R.string.anime_home_trending_row_label,
                    result.trending?.media,
                    viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                            + "?titleRes=${R.string.anime_home_trending_screen_title}"
                            + "&mediaType=${mediaType.name}"
                            + "&sort=${MediaSortOption.TRENDING}"
                ),
                RowInput(
                    "anime_popular_this_season",
                    R.string.anime_home_popular_this_season,
                    result.popularThisSeason?.media,
                    viewAllRoute = "${AnimeNavDestinations.SEASONAL.id}?type=${SeasonalViewModel.Type.THIS.name}"
                ),
                RowInput(
                    "anime_last_added",
                    R.string.anime_home_last_added,
                    result.lastAdded?.media,
                    viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                            + "?titleRes=${R.string.anime_home_last_added_screen_title}"
                            + "&mediaType=${mediaType.name}"
                            + "&sort=${MediaSortOption.ID}"
                ),
                RowInput(
                    "anime_popular_last_season",
                    R.string.anime_home_popular_last_season,
                    result.popularLastSeason?.media,
                    viewAllRoute = "${AnimeNavDestinations.SEASONAL.id}?type=${SeasonalViewModel.Type.LAST.name}"
                ),
                RowInput(
                    "anime_popular_next_season",
                    R.string.anime_home_popular_next_season,
                    result.popularNextSeason?.media,
                    viewAllRoute = "${AnimeNavDestinations.SEASONAL.id}?type=${SeasonalViewModel.Type.NEXT.name}"
                ),
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

        override suspend fun rows() = flowFromSuspend {
            val result = aniListApi.homeManga()
            listOf(
                RowInput(
                    "manga_trending",
                    R.string.anime_home_trending_row_label,
                    result.trending?.media,
                    viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                            + "?titleRes=${R.string.anime_home_trending_screen_title}"
                            + "&mediaType=${mediaType.name}"
                            + "&sort=${MediaSortOption.TRENDING}"
                ),
                RowInput(
                    "manga_last_added",
                    R.string.anime_home_last_added,
                    result.lastAdded?.media,
                    viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                            + "?titleRes=${R.string.anime_home_last_added_screen_title}"
                            + "&mediaType=${mediaType.name}"
                            + "&sort=${MediaSortOption.ID}"
                ),
                RowInput(
                    "manga_top_released_this_year",
                    R.string.anime_home_top_released_this_year,
                    result.topThisYear?.media,
                    viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                            + "?titleRes=${R.string.anime_home_top_released_this_year_title}"
                            + "&mediaType=${mediaType.name}"
                            + "&sort=${MediaSortOption.SCORE}"
                ),
            )
        }
    }

    data class RowInput(
        val id: String,
        val titleRes: Int,
        val list: List<MediaPreviewWithDescription?>?,
        val viewAllRoute: String? = null,
    )
}
