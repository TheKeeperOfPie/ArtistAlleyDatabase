package com.thekeeperofpie.artistalleydatabase.anime.news.ann

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import kotlin.time.Instant

internal const val ANIME_NEWS_NETWORK_ATOM_URL_PREFIX =
    "https://www.animenewsnetwork.com/news/atom.xml?ann-edition="

@Serializable
data class AnimeNewsNetworkFeed(
    @XmlElement
    val rights: String? = null,
    @XmlSerialName("entry")
    val entries: List<Entry> = emptyList(),
) {
    @Serializable
    data class Entry(
        @XmlElement
        val title: String? = null,
        @XmlSerialName("link")
        val link: Link? = null,
        @XmlElement
        val published: Instant? = null,
        @XmlElement
        val updated: Instant? = null,
        @XmlElement
        val summary: String? = null,
        @XmlSerialName("category")
        val categories: List<Category> = emptyList(),
    ) {
        @Serializable
        data class Link(
            @XmlSerialName("href")
            val url: String? = null,
        )

        @Serializable
        data class Category(
            @XmlSerialName("term")
            @Serializable(with = AnimeNewsNetworkCategory.Serializer::class)
            val value: AnimeNewsNetworkCategory = AnimeNewsNetworkCategory.UNKNOWN,
        )
    }
}
