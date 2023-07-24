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
import com.hoc081098.flowext.combine
import com.hoc081098.flowext.flowFromSuspend
import com.hoc081098.flowext.startWith
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
) : ViewModel() {

    var entry by mutableStateOf<AnimeHomeDataEntry?>(null)
    var errorResource by mutableStateOf<Pair<Int, Throwable?>?>(null)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            // TODO: Error handling for individual pieces
            combine(
                refreshUptimeMillis.mapLatest { rows() }.catch { emit(emptyList()) }
                    .startWith(item = null),
                statusController.allChanges(),
                ignoreList.updates,
                settings.showAdult,
                settings.showIgnored,
                current()
                    .mapLatest {
                        it?.getOrNull()
                            ?.find { it.status == MediaListStatus.CURRENT }
                            ?.entries
                            ?: emptyList()
                    }
                    .catch { emit(emptyList()) }
                    .startWith(
                        // If there's no user logged in, emit an empty list to hide the section
                        aniListApi.hasAuthToken.take(1)
                            .mapLatest { if (it) null else emptyList() }
                    ),
            ) { rows, statuses, ignoredIds, showAdult, showIgnored, current ->
                AnimeHomeDataEntry(
                    lists = rows?.map {
                        AnimeHomeDataEntry.RowData(
                            id = it.id,
                            titleRes = it.titleRes,
                            entries = it.list?.filterNotNull()?.map(AnimeHomeDataEntry::MediaEntry)
                                .orEmpty()
                                .mapNotNull {
                                    applyMediaFiltering(
                                        statuses = statuses,
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
                    current = current?.mapNotNull {
                        applyMediaFiltering(
                            statuses = statuses,
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
                )
            }
                .mapLatest { Result.success(it) }
                .catch { emit(Result.failure(it)) }
                .collectLatest {
                    withContext(CustomDispatchers.Main) {
                        if (it.isFailure) {
                            errorResource =
                                R.string.anime_home_error_loading_anime to it.exceptionOrNull()
                        } else {
                            entry = it.getOrNull()
                        }
                    }
                }
        }
    }

    protected abstract suspend fun rows(): List<RowInput>

    // It is faster to load the specific current list,
    // but this duplicates with the full user media lists
    // TODO: Make this more efficient when user list caching is available
    suspend fun current() = aniListApi.authedUser.flatMapLatest {
        if (it == null) {
            emptyFlow()
        } else {
            flowFromSuspend {
                aniListApi.userMediaList(
                    userId = it.id,
                    type = mediaType,
                    status = MediaListStatus.CURRENT
                ).lists
                    ?.filterNotNull()
                    ?.map(UserMediaListController::ListEntry)
                    .orEmpty()
            }
                .mapLatest { Result.success(it) }
                .catch { emit(Result.failure(it)) }
                .startWith(item = null)
        }
    }

    fun onLongClickEntry(media: MediaNavigationData) = ignoreList.toggle(media.id.toString())

    fun refresh() {
        val refresh = SystemClock.uptimeMillis()
        userMediaListController.refresh(mediaType)
        refreshUptimeMillis.value = refresh
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
