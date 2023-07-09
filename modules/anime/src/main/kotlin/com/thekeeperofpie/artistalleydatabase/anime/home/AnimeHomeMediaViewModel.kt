@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anilist.fragment.MediaPreviewWithDescription
import com.hoc081098.flowext.startWith
import com.rometools.rome.io.SyndFeedInput
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.seasonal.SeasonalViewModel
import com.thekeeperofpie.artistalleydatabase.anime.utils.AnimeNewsNetworkRegion
import com.thekeeperofpie.artistalleydatabase.anime.utils.AnimeNewsNetworkUtils
import com.thekeeperofpie.artistalleydatabase.anime.utils.CrunchyrollUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Date
import javax.inject.Inject

abstract class AnimeHomeMediaViewModel(
    protected val aniListApi: AuthedAniListApi,
    protected val settings: AnimeSettings,
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

    abstract suspend fun media(): List<RowInput>

    fun onLongClickEntry(entry: AnimeHomeDataEntry.MediaEntry) {
        val mediaId = entry.media.id.toString()
        val ignored = !entry.ignored
        ignoreList.set(mediaId, ignored)
        entry.ignored = ignored
    }

    @HiltViewModel
    class Anime @Inject constructor(
        application: Application,
        aniListApi: AuthedAniListApi,
        settings: AnimeSettings,
        ignoreList: AnimeMediaIgnoreList,
        okHttpClient: OkHttpClient,
    ) : AnimeHomeMediaViewModel(aniListApi, settings, ignoreList) {

        var newsRegion = settings.animeNewsNetworkRegion.map {
            try {
                AnimeNewsNetworkRegion.valueOf(it)
            } catch (ignored: Throwable) {
                AnimeNewsNetworkRegion.USA_CANADA
            }
        }

        var news by mutableStateOf<List<NewsArticleEntry>>(emptyList())

        init {
            viewModelScope.launch(CustomDispatchers.IO) {
                val animeNewsNetwork = newsRegion
                    .mapLatest {
                        fetchFeed(
                            okHttpClient = okHttpClient,
                            url = "${AnimeNewsNetworkUtils.NEWS_ATOM_URL_PREFIX}${it.id}"
                        )
                    }
                    .catch {}
                    .startWith(item = emptyList())

                val crunchyroll = flow {
                    emit(
                        fetchFeed(
                            okHttpClient = okHttpClient,
                            url = CrunchyrollUtils.NEW_RSS_URL,
                        )
                    )
                }
                    .catch {}
                    .startWith(item = emptyList())

                combine(animeNewsNetwork, crunchyroll, List<NewsArticleEntry>::plus)
                    .map { it.sortedByDescending { it.date } }
                    .collectLatest {
                        withContext(CustomDispatchers.Main) {
                            news = it
                        }
                    }
            }
        }

        private fun fetchFeed(okHttpClient: OkHttpClient, url: String) = okHttpClient.newCall(
            Request.Builder()
                .url(url)
                .get()
                .build()
        ).execute().body.charStream().use {
            val feed = SyndFeedInput().build(it)
            val iconUrl = feed.foreignMarkup
                .find { it.namespacePrefix == "snf" && it.name == "logo" }
                ?.children
                ?.find { it.name == "url" }
                ?.textNormalize
            feed.entries.map {
                val imageUrl = it.foreignMarkup
                    .find { it.namespacePrefix == "media" && it.name == "thumbnail" }
                    ?.getAttributeValue("url")
                NewsArticleEntry(
                    icon = feed.icon?.url ?: iconUrl,
                    image = imageUrl,
                    title = it.title,
                    // Add 3 newlines to force text height measurement to be at least 3 lines tall
                    description = it.description?.value.orEmpty() + "\n\n\n",
                    link = it.link,
                    copyright = feed.copyright,
                    date = it.publishedDate,
                )
            }
        }

        fun onNewsRegionChanged(region: AnimeNewsNetworkRegion) {
            settings.animeNewsNetworkRegion.value = region.id
        }

        override suspend fun media(): List<RowInput> {
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

        data class NewsArticleEntry(
            val icon: String?,
            val image: String?,
            val title: String?,
            val description: String?,
            val link: String?,
            val copyright: String?,
            val date: Date,
        )
    }

    @HiltViewModel
    class Manga @Inject constructor(
        aniListApi: AuthedAniListApi,
        settings: AnimeSettings,
        ignoreList: AnimeMediaIgnoreList,
    ) : AnimeHomeMediaViewModel(aniListApi, settings, ignoreList) {
        override suspend fun media(): List<RowInput> {
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
