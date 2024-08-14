package com.thekeeperofpie.artistalleydatabase.news

import com.thekeeperofpie.artistalleydatabase.news.ann.AnimeNewsNetworkCategory
import com.thekeeperofpie.artistalleydatabase.news.ann.AnimeNewsNetworkFeed
import com.thekeeperofpie.artistalleydatabase.news.cr.CrunchyrollNewsCategory
import com.thekeeperofpie.artistalleydatabase.news.cr.CrunchyrollNewsFeed
import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig

internal object NewsXml {

    @OptIn(ExperimentalXmlUtilApi::class)
    internal val xml = XML {
        defaultPolicy {
            unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
        }
    }

    fun parseAnimeNewsNetworkFeed(source: Source): List<AnimeNewsEntry<AnimeNewsNetworkCategory>> {
        val feed = xml.decodeFromString<AnimeNewsNetworkFeed>(source.readString())
        return feed.entries.mapIndexed { index, entry ->
            AnimeNewsEntry(
                id = "${AnimeNewsType.ANIME_NEWS_NETWORK} - $index",
                type = AnimeNewsType.ANIME_NEWS_NETWORK,
                icon = null,
                image = null,
                title = entry.title,
                description = entry.summary,
                link = entry.link?.url,
                copyright = feed.rights,
                date = entry.updated ?: entry.published,
                categories = entry.categories.map { it.value },
            )
        }
    }

    fun parseCrunchyrollNewsFeed(source: Source): List<AnimeNewsEntry<CrunchyrollNewsCategory>> {
        val feed = xml.decodeFromString<CrunchyrollNewsFeed>(source.readString())
        val channel = feed.channel ?: return emptyList()
        return channel.items?.mapIndexed { index, entry ->
            AnimeNewsEntry(
                id = "${AnimeNewsType.CRUNCHYROLL} - $index",
                type = AnimeNewsType.CRUNCHYROLL,
                icon = channel.logo?.url,
                image = entry.thumbnail?.url,
                title = entry.title,
                description = entry.content,
                link = entry.link,
                copyright = channel.copyright,
                date = entry.pubDate,
                categories = listOfNotNull(entry.category),
            )
        }.orEmpty()
    }
}
