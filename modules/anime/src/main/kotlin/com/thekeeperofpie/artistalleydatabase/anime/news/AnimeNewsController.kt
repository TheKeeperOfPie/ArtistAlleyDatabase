package com.thekeeperofpie.artistalleydatabase.anime.news

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.hoc081098.flowext.combine
import com.hoc081098.flowext.startWith
import com.rometools.rome.io.SyndFeedInput
import com.thekeeperofpie.artistalleydatabase.android_utils.ScopedApplication
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.compose.filter.FilterIncludeExcludeState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeNewsController(
    private val scopedApplication: ScopedApplication,
    private val okHttpClient: OkHttpClient,
    private val settings: AnimeSettings,
) {
    private var job: Job? = null
    private val news = MutableStateFlow<List<AnimeNewsArticleEntry>>(emptyList())
    private var newsDateDescending by mutableStateOf<List<AnimeNewsArticleEntry>>(emptyList())
    private val refreshUptimeMillis = MutableStateFlow(-1L)

    fun news(): MutableStateFlow<List<AnimeNewsArticleEntry>> {
        startJobIfNeeded()
        return news
    }

    fun newsDateDescending(): List<AnimeNewsArticleEntry> {
        startJobIfNeeded()
        return newsDateDescending
    }

    private fun startJobIfNeeded() {
        if (job != null) return
        job = scopedApplication.scope.launch(CustomDispatchers.IO) {
            val animeNewsNetwork = settings.animeNewsNetworkRegion
                .mapLatest {
                    fetchFeed(
                        type = AnimeNewsType.ANIME_NEWS_NETWORK,
                        okHttpClient = okHttpClient,
                        url = "${AnimeNewsNetworkUtils.NEWS_ATOM_URL_PREFIX}${it.id}"
                    )
                }
                .catch {}
                .startWith(item = emptyList())

            val crunchyroll = flow {
                emit(
                    fetchFeed(
                        type = AnimeNewsType.CRUNCHYROLL,
                        okHttpClient = okHttpClient,
                        url = CrunchyrollNewsUtils.NEW_RSS_URL,
                    )
                )
            }
                .catch {}
                .startWith(item = emptyList())

            combine(
                refreshUptimeMillis,
                animeNewsNetwork,
                crunchyroll,
                settings.animeNewsNetworkCategoriesIncluded,
                settings.animeNewsNetworkCategoriesExcluded,
                settings.crunchyrollNewsCategoriesIncluded,
                settings.crunchyrollNewsCategoriesExcluded,
                ::Params
            )
                .mapLatest {
                    mutableListOf<AnimeNewsArticleEntry>().apply {
                        this += FilterIncludeExcludeState.applyFiltering(
                            includes = it.animeNewsNetworkCategoriesIncluded.map { it.id },
                            excludes = it.animeNewsNetworkCategoriesExcluded.map { it.id },
                            list = it.animeNewsNetworkEntries,
                            transform = { it.categories }
                        )
                        this += FilterIncludeExcludeState.applyFiltering(
                            includes = it.crunchyrollNewsCategoriesIncluded.map { it.id },
                            excludes = it.crunchyrollNewsCategoriesExcluded.map { it.id },
                            list = it.crunchyrollNewsEntries,
                            transform = { it.categories }
                        )
                    }
                }
                .collectLatest {
                    news.emit(it)
                    val sortedDateDescending = it.sortedByDescending { it.date }
                    withContext(CustomDispatchers.Main) {
                        newsDateDescending = sortedDateDescending
                    }
                }
        }
    }

    private fun fetchFeed(type: AnimeNewsType, okHttpClient: OkHttpClient, url: String) =
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
                    categories = entry.categories.map { it.name }
                        .ifEmpty { listOf(AnimeNewsNetworkCategory.UNKNOWN.id) },
                )
            }
        }

    fun refresh() {
        refreshUptimeMillis.value = SystemClock.uptimeMillis()
    }

    private data class Params(
        val refreshMillis: Long,
        val animeNewsNetworkEntries: List<AnimeNewsArticleEntry>,
        val crunchyrollNewsEntries: List<AnimeNewsArticleEntry>,
        val animeNewsNetworkCategoriesIncluded: List<AnimeNewsNetworkCategory>,
        val animeNewsNetworkCategoriesExcluded: List<AnimeNewsNetworkCategory>,
        val crunchyrollNewsCategoriesIncluded: List<CrunchyrollNewsCategory>,
        val crunchyrollNewsCategoriesExcluded: List<CrunchyrollNewsCategory>,
    )
}
