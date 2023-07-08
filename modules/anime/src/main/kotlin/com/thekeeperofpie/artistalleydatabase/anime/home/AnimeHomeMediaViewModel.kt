package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.MediaPreviewWithDescription
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class AnimeHomeMediaViewModel(
    protected val aniListApi: AuthedAniListApi,
    private val settings: AnimeSettings,
    private val ignoreList: AnimeMediaIgnoreList,
) : ViewModel() {

    var entry by mutableStateOf<AnimeHomeDataEntry?>(null)
    var errorResource by mutableStateOf<Pair<Int, Throwable?>?>(null)

    private val refreshUptimeMillis = MutableStateFlow(-1)

    init {
        viewModelScope.launch(CustomDispatchers.IO) {
            combine(
                refreshUptimeMillis,
                settings.ignoredAniListMediaIds,
                settings.showIgnored,
                ::Triple
            )
                .map { (_, ignoredIds, showIgnored) ->
                    AnimeHomeDataEntry(
                        ignoredIds = ignoredIds,
                        showIgnored = showIgnored,
                        lists = media(),
                    )
                }
                .map(Result.Companion::success)
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

    abstract suspend fun media(): List<Triple<String, Int, List<MediaPreviewWithDescription?>?>>

    fun onLongClickEntry(entry: AnimeHomeDataEntry.MediaEntry) {
        val mediaId = entry.media.id.toString()
        val ignored = !entry.ignored
        ignoreList.set(mediaId, ignored)
        entry.ignored = ignored
    }

    @HiltViewModel
    class Anime @Inject constructor(
        aniListApi: AuthedAniListApi,
        settings: AnimeSettings,
        ignoreList: AnimeMediaIgnoreList,
    ) : AnimeHomeMediaViewModel(aniListApi, settings, ignoreList) {
        override suspend fun media(): List<Triple<String, Int, List<MediaPreviewWithDescription?>?>> {
            val lists = aniListApi.homeAnime()
            return listOf(
                Triple(
                    "anime_trending",
                    R.string.anime_home_trending,
                    lists.trending?.media,
                ),
                Triple(
                    "anime_popular_this_season",
                    R.string.anime_home_popular_this_season,
                    lists.popularThisSeason?.media,
                ),
                Triple(
                    "anime_popular_last_season",
                    R.string.anime_home_popular_last_season,
                    lists.popularLastSeason?.media,
                ),
                Triple(
                    "anime_popular_next_season",
                    R.string.anime_home_popular_next_season,
                    lists.popularNextSeason?.media,
                ),
                Triple(
                    "anime_popular",
                    R.string.anime_home_popular,
                    lists.popular?.media,
                ),
                Triple("anime_top", R.string.anime_home_top, lists.top?.media),
            )
        }
    }

    @HiltViewModel
    class Manga @Inject constructor(
        aniListApi: AuthedAniListApi,
        settings: AnimeSettings,
        ignoreList: AnimeMediaIgnoreList,
    ) : AnimeHomeMediaViewModel(aniListApi, settings, ignoreList) {
        override suspend fun media(): List<Triple<String, Int, List<MediaPreviewWithDescription?>?>> {
            val lists = aniListApi.homeManga()
            return listOf(
                Triple(
                    "manga_trending",
                    R.string.anime_home_trending,
                    lists.trending?.media
                ),
                Triple(
                    "manga_popular",
                    R.string.anime_home_popular,
                    lists.popular?.media
                ),
                Triple("manga_top", R.string.anime_home_top, lists.top?.media),
            )
        }
    }
}
