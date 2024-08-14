package com.thekeeperofpie.artistalleydatabase.news

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import com.thekeeperofpie.artistalleydatabase.news.ann.ANIME_NEWS_NETWORK_ATOM_URL_PREFIX
import com.thekeeperofpie.artistalleydatabase.news.cr.CRUNCHYROLL_NEWS_RSS_URL
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.FilterIncludeExcludeState
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeNewsController(
    private val scope: CoroutineScope,
    private val httpClient: HttpClient,
    private val settings: NewsSettings,
) {
    companion object {
        private const val TAG = "AnimeNewsController"
    }

    private var job: Job? = null
    private val news = MutableStateFlow<List<AnimeNewsEntry<*>>?>(null)
    private var newsDateDescending by mutableStateOf<List<AnimeNewsEntry<*>>?>(null)
    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun news(): MutableStateFlow<List<AnimeNewsEntry<*>>?> {
        startJobIfNeeded()
        return news
    }

    fun newsDateDescending(): List<AnimeNewsEntry<*>>? {
        startJobIfNeeded()
        return newsDateDescending
    }

    // TODO: Loading and error indicator
    private fun startJobIfNeeded() {
        if (job != null) return
        job = scope.launch(CustomDispatchers.IO) {
            val animeNewsNetwork =
                combine(settings.animeNewsNetworkRegion, refreshUptimeMillis, ::Pair)
                    .flatMapLatest { (region) ->
                        flow {
                            val result = async {
                                runCatching {
                                    fetchFeed(
                                        type = AnimeNewsType.ANIME_NEWS_NETWORK,
                                        url = "$ANIME_NEWS_NETWORK_ATOM_URL_PREFIX${region.id}",
                                    )
                                }
                            }
                            select {
                                result.onAwait { emit(it) }
                                onTimeout(2.seconds) {
                                    emit(null)
                                    emit(result.await())
                                }
                            }
                        }
                    }
                    .map { it?.getOrNull() }
                    .catch {
                        Logger.e(TAG, it) { "Error fetching animeNewsNetwork" }
                        emit(null)
                    }

            val crunchyroll = refreshUptimeMillis
                .flatMapLatest {
                    flow {
                        val result = async {
                            runCatching {
                                fetchFeed(
                                    type = AnimeNewsType.CRUNCHYROLL,
                                    url = CRUNCHYROLL_NEWS_RSS_URL,
                                )
                            }
                        }
                        select {
                            result.onAwait { emit(it) }
                            onTimeout(2.seconds) {
                                emit(null)
                                emit(result.await())
                            }
                        }
                    }
                }
                .map { it?.getOrNull() }
                .catch {
                    Logger.e(TAG, it) { "Error fetching crunchyroll" }
                    emit(null)
                }

            val animeNewsNetworkFiltered = combine(
                animeNewsNetwork,
                settings.animeNewsNetworkCategoriesIncluded,
                settings.animeNewsNetworkCategoriesExcluded,
            ) { news, included, excluded ->
                news ?: return@combine null
                FilterIncludeExcludeState.applyFiltering(
                    includes = included,
                    excludes = excluded,
                    list = news,
                    transform = { it.categories },
                    mustContainAll = false,
                )
            }

            val crunchyrollFiltered = combine(
                crunchyroll,
                settings.crunchyrollNewsCategoriesIncluded,
                settings.crunchyrollNewsCategoriesExcluded,
            ) { news, included, excluded ->
                news ?: return@combine null
                FilterIncludeExcludeState.applyFiltering(
                    includes = included,
                    excludes = excluded,
                    list = news,
                    transform = { it.categories },
                    mustContainAll = false,
                )
            }

            combine(
                animeNewsNetworkFiltered,
                crunchyrollFiltered,
            ) { annNews, crNews ->
                val combined = (annNews.orEmpty() + crNews.orEmpty())
                combined.takeIf { (annNews != null && crNews != null) || it.isNotEmpty() }
            }
                .flowOn(CustomDispatchers.IO)
                .collectLatest {
                    news.emit(it)
                    val sortedDateDescending = it?.sortedByDescending { it.date }
                    withContext(CustomDispatchers.Main) {
                        newsDateDescending = sortedDateDescending
                    }
                }
        }
    }

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

    fun refresh() {
        refreshUptimeMillis.value = Clock.System.now().toEpochMilliseconds()
    }
}
