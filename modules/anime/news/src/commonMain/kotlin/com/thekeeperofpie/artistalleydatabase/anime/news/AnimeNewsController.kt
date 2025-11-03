package com.thekeeperofpie.artistalleydatabase.anime.news

import artistalleydatabase.modules.anime.news.generated.resources.Res
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_error_loading_crunchyroll
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.anime.news.ann.ANIME_NEWS_NETWORK_ATOM_URL_PREFIX
import com.thekeeperofpie.artistalleydatabase.anime.news.cr.CRUNCHYROLL_NEWS_RSS_URL
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readBuffer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
@Inject
class AnimeNewsController(
    private val scope: ApplicationScope,
    private val httpClient: HttpClient,
    private val settings: NewsSettings,
) {
    companion object {
        private const val TAG = "AnimeNewsController"
    }

    private val refresh = RefreshFlow()

    private val annNews =
        flowForRefreshableContent(refresh, Res.string.anime_news_error_loading_crunchyroll) {
            settings.animeNewsNetworkRegion.map {
                fetchFeed(
                    type = AnimeNewsType.ANIME_NEWS_NETWORK,
                    url = "$ANIME_NEWS_NETWORK_ATOM_URL_PREFIX${it.id}",
                )
            }
        }
            .flatMapLatest { result ->
                combine(
                    settings.animeNewsNetworkCategoriesIncluded,
                    settings.animeNewsNetworkCategoriesExcluded,
                ) { included, excluded ->
                    result.transformResult {
                        FilterIncludeExcludeState.applyFiltering(
                            includes = included,
                            excludes = excluded,
                            list = it,
                            transform = { it.categories },
                            mustContainAll = false,
                        )
                    }
                }
            }

    private val crNews =
        flowForRefreshableContent(refresh, Res.string.anime_news_error_loading_crunchyroll) {
            flowFromSuspend {
                fetchFeed(
                    type = AnimeNewsType.CRUNCHYROLL,
                    url = CRUNCHYROLL_NEWS_RSS_URL,
                )
            }
        }
            .flatMapLatest { result ->
                combine(
                    settings.crunchyrollNewsCategoriesIncluded,
                    settings.crunchyrollNewsCategoriesExcluded,
                ) { included, excluded ->
                    result.transformResult {
                        FilterIncludeExcludeState.applyFiltering(
                            includes = included,
                            excludes = excluded,
                            list = it,
                            transform = { it.categories },
                            mustContainAll = false,
                        )
                    }
                }
            }


    val news = combine(annNews, crNews) { annNews, crNews ->
        LoadingResult.combine(annNews, crNews) { it.flatten() }
    }
        .flowOn(CustomDispatchers.IO)
        .stateIn(scope, SharingStarted.Lazily, LoadingResult.loading())

    var newsDateDescending = news.mapLatest {
        it.transformResult {
            it.sortedByDescending { it.date }
        }
    }
        .flowOn(CustomDispatchers.IO)
        .stateIn(scope, SharingStarted.Lazily, LoadingResult.loading())

    private suspend fun fetchFeed(
        type: AnimeNewsType,
        url: String,
    ): List<AnimeNewsEntry<*>> {
        val source = httpClient.get(url).bodyAsChannel().readBuffer()
        return when (type) {
            AnimeNewsType.ANIME_NEWS_NETWORK -> NewsXml.parseAnimeNewsNetworkFeed(source)
            AnimeNewsType.CRUNCHYROLL -> NewsXml.parseCrunchyrollNewsFeed(source)
        }
    }

    fun refresh() = refresh.refresh()
}
