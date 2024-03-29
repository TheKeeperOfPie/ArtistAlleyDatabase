package com.thekeeperofpie.artistalleydatabase.anime.news

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rometools.rome.feed.synd.SyndCategory
import com.rometools.rome.io.SyndFeedInput
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AnimeNewsController(
    private val scopedApplication: ScopedApplication,
    private val okHttpClient: OkHttpClient,
    private val settings: AnimeSettings,
) {
    companion object {
        private const val TAG = "AnimeNewsController"
    }

    private var job: Job? = null
    private val news = MutableStateFlow<ImmutableList<AnimeNewsArticleEntry<*>>?>(null)
    private var newsDateDescending by mutableStateOf<ImmutableList<AnimeNewsArticleEntry<*>>?>(null)
    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun news(): MutableStateFlow<ImmutableList<AnimeNewsArticleEntry<*>>?> {
        startJobIfNeeded()
        return news
    }

    fun newsDateDescending(): ImmutableList<AnimeNewsArticleEntry<*>>? {
        startJobIfNeeded()
        return newsDateDescending
    }

    // TODO: Loading and error indicator
    private fun startJobIfNeeded() {
        if (job != null) return
        job = scopedApplication.scope.launch(CustomDispatchers.IO) {
            val animeNewsNetwork =
                combine(settings.animeNewsNetworkRegion, refreshUptimeMillis, ::Pair)
                    .flatMapLatest { (region) ->
                        flow {
                            val result = async {
                                runCatching {
                                    fetchFeed(
                                        type = AnimeNewsType.ANIME_NEWS_NETWORK,
                                        okHttpClient = okHttpClient,
                                        url = "${AnimeNewsNetworkUtils.NEWS_ATOM_URL_PREFIX}${region.id}",
                                        mapCategories = { category ->
                                            AnimeNewsNetworkCategory.entries.find { it.id == category.name }
                                                ?: AnimeNewsNetworkCategory.UNKNOWN
                                        },
                                        ifEmpty = AnimeNewsNetworkCategory.UNKNOWN,
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
                        Log.e(TAG, "Error fetching animeNewsNetwork", it)
                        emit(null)
                    }

            val crunchyroll = refreshUptimeMillis
                .flatMapLatest {
                    flow {
                        val result = async {
                            runCatching {
                                fetchFeed(
                                    type = AnimeNewsType.CRUNCHYROLL,
                                    okHttpClient = okHttpClient,
                                    url = CrunchyrollNewsUtils.NEW_RSS_URL,
                                    mapCategories = { category ->
                                        CrunchyrollNewsCategory.entries.find { it.id == category.name }
                                            ?: CrunchyrollNewsCategory.UNKNOWN
                                    },
                                    ifEmpty = CrunchyrollNewsCategory.UNKNOWN,
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
                    Log.e(TAG, "Error fetching crunchyroll", it)
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
                val combined = (annNews.orEmpty() + crNews.orEmpty()).toImmutableList()
                combined.takeIf { (annNews != null && crNews != null) || it.isNotEmpty() }
            }
                .flowOn(CustomDispatchers.IO)
                .collectLatest {
                    news.emit(it)
                    val sortedDateDescending = it?.sortedByDescending { it.date }?.toImmutableList()
                    withContext(CustomDispatchers.Main) {
                        newsDateDescending = sortedDateDescending
                    }
                }
        }
    }

    private fun <Category> fetchFeed(
        type: AnimeNewsType,
        okHttpClient: OkHttpClient,
        url: String,
        mapCategories: (SyndCategory) -> Category,
        ifEmpty: Category,
    ) =
        okHttpClient.newCall(
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
            feed.entries.mapIndexed { index, entry ->
                val imageUrl = entry.foreignMarkup
                    .find { it.namespacePrefix == "media" && it.name == "thumbnail" }
                    ?.getAttributeValue("url")
                AnimeNewsArticleEntry(
                    id = "$type-$index",
                    type = type,
                    icon = feed.icon?.url ?: iconUrl,
                    image = imageUrl,
                    title = entry.title,
                    // Add 3 newlines to force text height measurement to be at least 3 lines tall
                    description = entry.description?.value.orEmpty() + "\n\n\n",
                    link = entry.link,
                    copyright = feed.copyright,
                    date = entry.publishedDate,
                    categories = entry.categories
                        .map(mapCategories)
                        .ifEmpty { listOf(ifEmpty) },
                )
            }
        }

    fun refresh() {
        refreshUptimeMillis.value = SystemClock.uptimeMillis()
    }
}
