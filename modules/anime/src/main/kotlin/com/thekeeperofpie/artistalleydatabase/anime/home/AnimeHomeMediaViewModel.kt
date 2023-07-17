@file:OptIn(ExperimentalCoroutinesApi::class)

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
import com.hoc081098.flowext.combine
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
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class AnimeHomeMediaViewModel(
    protected val aniListApi: AuthedAniListApi,
    protected val settings: AnimeSettings,
    private val ignoreList: AnimeMediaIgnoreList,
    private val userMediaListController: UserMediaListController,
    private val statusController: MediaListStatusController,
    @StringRes val currentHeaderTextRes: Int,
) : ViewModel() {

    var entry by mutableStateOf<AnimeHomeDataEntry?>(null)
    var errorResource by mutableStateOf<Pair<Int, Throwable?>?>(null)

    private val refreshUptimeMillis = MutableStateFlow(-1L)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                refreshUptimeMillis.mapLatest { rows() }.startWith(item = null),
                statusController.allChanges(),
                ignoreList.updates,
                settings.showAdult,
                settings.showIgnored,
                userMediaListController.data
                    .mapLatest {
                        it?.let { current(it) }?.getOrNull()
                            ?.find { it.status == MediaListStatus.CURRENT }
                            ?.entries
                            ?: emptyList()
                    }
                    .startWith(item = null),
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
                                UserMediaListController.Entry.MediaEntry(
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

    protected abstract suspend fun current(
        entry: UserMediaListController.Entry,
    ): Result<List<UserMediaListController.Entry.ListEntry>>?

    fun onLongClickEntry(media: MediaNavigationData) = ignoreList.toggle(media.id.toString())

    fun refresh() {
        val refresh = SystemClock.uptimeMillis()
        userMediaListController.refresh.value = refresh
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
    ) {

        override suspend fun current(entry: UserMediaListController.Entry) = entry.anime

        override suspend fun rows(): List<RowInput> {
            val lists = aniListApi.homeAnime()
            return listOf(
                RowInput(
                    "anime_trending",
                    R.string.anime_home_trending,
                    lists.trending?.media,
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
                    R.string.anime_home_popular,
                    lists.popular?.media,
                ),
                RowInput("anime_top", R.string.anime_home_top, lists.top?.media),
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
    ) {

        override suspend fun current(entry: UserMediaListController.Entry) = entry.manga

        override suspend fun rows(): List<RowInput> {
            val lists = aniListApi.homeManga()
            return listOf(
                RowInput(
                    "manga_trending",
                    R.string.anime_home_trending,
                    lists.trending?.media
                ),
                RowInput(
                    "manga_popular",
                    R.string.anime_home_popular,
                    lists.popular?.media
                ),
                RowInput("manga_top", R.string.anime_home_top, lists.top?.media),
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
