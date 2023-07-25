package com.thekeeperofpie.artistalleydatabase.anime.home

import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.UserMediaListController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.MediaSortOption
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AnimeHomeMediaViewModel(
    protected val aniListApi: AuthedAniListApi,
    protected val settings: AnimeSettings,
    private val ignoreList: AnimeMediaIgnoreList,
    protected val userMediaListController: UserMediaListController,
    private val statusController: MediaListStatusController,
    @StringRes val currentHeaderTextRes: Int,
    protected val mediaType: MediaType,
    @StringRes private val errorTextRes: Int,
) : ViewModel() {

    var entry by mutableStateOf<LoadingResult<AnimeHomeDataEntry>>(LoadingResult.loading())
    var current by mutableStateOf<LoadingResult<List<UserMediaListController.MediaEntry>>>(
        LoadingResult.loading()
    )

    private val refresh = MutableStateFlow(-1L)

    init {
        collectCurrent()
        collectMedia()
    }

    protected abstract suspend fun rows(): List<RowInput>

    // It is faster to load the specific current list,
    // but this duplicates with the full user media lists
    // TODO: Make this more efficient when user list caching is available
    suspend fun current() = aniListApi.authedUser.flatMapLatest {
        if (it == null) {
            emptyFlow()
        } else {
            aniListApi.userMediaList(
                userId = it.id,
                type = mediaType,
                status = MediaListStatus.CURRENT
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

    fun onLongClickEntry(media: MediaNavigationData) = ignoreList.toggle(media.id.toString())

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
                        statusController.allChanges(),
                        ignoreList.updates,
                        settings.showAdult,
                        settings.showIgnored,
                    ) { mediaStatusUpdates, ignoredIds, showAdult, showIgnored ->
                        current.copy(
                            result = current.result?.mapNotNull {
                                applyMediaFiltering(
                                    statuses = mediaStatusUpdates,
                                    ignoredIds = ignoredIds,
                                    showAdult = showAdult,
                                    showIgnored = showIgnored,
                                    entry = it,
                                    transform = { it },
                                    media = it.media,
                                    copy = { mediaListStatus, progress, progressVolumes, ignored ->
                                        UserMediaListController.MediaEntry(
                                            media = media,
                                            mediaListStatus = mediaListStatus,
                                            progress = progress,
                                            progressVolumes = progressVolumes,
                                            ignored = ignored,
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
            flowForRefreshableContent(refresh, errorTextRes) { flowFromSuspend { rows() } }
                .flatMapLatest { mediaResult ->
                    combine(
                        statusController.allChanges(),
                        ignoreList.updates,
                        settings.showAdult,
                        settings.showIgnored,
                    ) { mediaStatusUpdates, ignoredIds, showAdult, showIgnored ->
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
                                                    ignoredIds = ignoredIds,
                                                    showAdult = showAdult,
                                                    showIgnored = showIgnored,
                                                    entry = it,
                                                    transform = { it },
                                                    media = it.media,
                                                    copy = { mediaListStatus, progress, progressVolumes, ignored ->
                                                        AnimeHomeDataEntry.MediaEntry(
                                                            media = media,
                                                            mediaListStatus = mediaListStatus,
                                                            progress = progress,
                                                            progressVolumes = progressVolumes,
                                                            ignored = ignored,
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

    @HiltViewModel
    class Anime @Inject constructor(
        aniListApi: AuthedAniListApi,
        settings: AnimeSettings,
        ignoreList: AnimeMediaIgnoreList,
        userMediaListController: UserMediaListController,
        statusController: MediaListStatusController,
    ) : AnimeHomeMediaViewModel(
        aniListApi = aniListApi,
        settings = settings,
        ignoreList = ignoreList,
        userMediaListController = userMediaListController,
        statusController = statusController,
        currentHeaderTextRes = R.string.anime_home_anime_current_header,
        mediaType = MediaType.ANIME,
        errorTextRes = R.string.anime_home_error_loading_anime,
    ) {

        override suspend fun rows(): List<RowInput> {
            val lists = aniListApi.homeAnime()
            return listOf(
                RowInput(
                    "anime_trending",
                    R.string.anime_home_trending_row_label,
                    lists.trending?.media,
                    viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                            + "?titleRes=${R.string.anime_home_trending_screen_title}"
                            + "&mediaType=${mediaType.name}"
                            + "&sort=${MediaSortOption.TRENDING}"
                ),
                RowInput(
                    "anime_popular_this_season",
                    R.string.anime_home_popular_this_season,
                    lists.popularThisSeason?.media,
                    viewAllRoute = "${AnimeNavDestinations.SEASONAL.id}?type=${SeasonalViewModel.Type.THIS.name}"
                ),
                RowInput(
                    "anime_popular_last_season",
                    R.string.anime_home_popular_last_season,
                    lists.popularLastSeason?.media,
                    viewAllRoute = "${AnimeNavDestinations.SEASONAL.id}?type=${SeasonalViewModel.Type.LAST.name}"
                ),
                RowInput(
                    "anime_popular_next_season",
                    R.string.anime_home_popular_next_season,
                    lists.popularNextSeason?.media,
                    viewAllRoute = "${AnimeNavDestinations.SEASONAL.id}?type=${SeasonalViewModel.Type.NEXT.name}"
                ),
                RowInput(
                    "anime_popular",
                    R.string.anime_home_popular_row_label,
                    lists.popular?.media,
                    viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                            + "?titleRes=${R.string.anime_home_popular_screen_title}"
                            + "&mediaType=${mediaType.name}"
                            + "&sort=${MediaSortOption.POPULARITY}"
                ),
                RowInput(
                    "anime_top", R.string.anime_home_top_row_label,
                    lists.top?.media,
                    viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                            + "?titleRes=${R.string.anime_home_top_screen_title}"
                            + "&mediaType=${mediaType.name}"
                            + "&sort=${MediaSortOption.SCORE}"
                ),
            )
        }
    }

    @HiltViewModel
    class Manga @Inject constructor(
        aniListApi: AuthedAniListApi,
        settings: AnimeSettings,
        ignoreList: AnimeMediaIgnoreList,
        userMediaListController: UserMediaListController,
        statusController: MediaListStatusController,
    ) : AnimeHomeMediaViewModel(
        aniListApi = aniListApi,
        settings = settings,
        ignoreList = ignoreList,
        userMediaListController = userMediaListController,
        statusController = statusController,
        currentHeaderTextRes = R.string.anime_home_manga_current_header,
        mediaType = MediaType.MANGA,
        errorTextRes = R.string.anime_home_error_loading_manga,
    ) {

        override suspend fun rows(): List<RowInput> {
            val lists = aniListApi.homeManga()
            return listOf(
                RowInput(
                    "manga_trending",
                    R.string.anime_home_trending_row_label,
                    lists.trending?.media,
                    viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                            + "?titleRes=${R.string.anime_home_trending_screen_title}"
                            + "&mediaType=${mediaType.name}"
                            + "&sort=${MediaSortOption.TRENDING}"
                ),
                RowInput(
                    "manga_popular",
                    R.string.anime_home_popular_row_label,
                    lists.popular?.media,
                    viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                            + "?titleRes=${R.string.anime_home_popular_screen_title}"
                            + "&mediaType=${mediaType.name}"
                            + "&sort=${MediaSortOption.POPULARITY}"
                ),
                RowInput(
                    "manga_top", R.string.anime_home_top_row_label,
                    lists.top?.media,
                    viewAllRoute = AnimeNavDestinations.SEARCH_MEDIA.id
                            + "?titleRes=${R.string.anime_home_top_screen_title}"
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
