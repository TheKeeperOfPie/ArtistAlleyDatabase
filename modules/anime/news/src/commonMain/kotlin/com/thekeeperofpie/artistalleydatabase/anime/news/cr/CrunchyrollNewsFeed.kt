package com.thekeeperofpie.artistalleydatabase.anime.news.cr

import com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.TrimmingStringSerializer
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.format
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.serialization.XmlCData
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import kotlin.time.Instant

internal const val CRUNCHYROLL_NEWS_RSS_URL =
    "https://cr-news-api-service.prd.crunchyrollsvc.com/v1/en-US/rss"

@Serializable
data class CrunchyrollNewsFeed(
    @XmlSerialName("channel")
    val channel: Channel? = null,
) {
    @Serializable
    data class Channel(
        @XmlElement
        val copyright: String? = null,
        @XmlSerialName(namespace = "http://www.smartnews.be/snf", prefix = "snf", value = "logo")
        val logo: Logo? = null,
        @XmlSerialName("item")
        val items: List<Item>? = emptyList(),
    ) {
        @Serializable
        data class Logo(
            @XmlSerialName(namespace = "", prefix = "", value = "url")
            @XmlElement
            val url: String? = null,
        )

        @Serializable
        data class Item(
            @XmlElement
            val title: String? = null,
            @XmlElement
            val author: String? = null,
            @XmlElement
            @Serializable(with = CrunchyrollNewsCategory.Serializer::class)
            val category: CrunchyrollNewsCategory = CrunchyrollNewsCategory.UNKNOWN,
            @XmlElement
            val description: String? = null,
            @Serializable(with = TrimmingStringSerializer::class)
            @XmlElement
            @XmlSerialName(
                namespace = "http://purl.org/rss/1.0/modules/content/",
                prefix = "content",
                value = "encoded"
            )
            @XmlCData
            val content: String? = null,
            @XmlElement
            @XmlSerialName(
                namespace = "http://search.yahoo.com/mrss/",
                prefix = "media",
                value = "thumbnail"
            )
            val thumbnail: Thumbnail? = null,
            @Serializable(with = InstantRfc1123Serializer::class)
            @XmlElement
            val pubDate: Instant? = null,
            @XmlElement
            val link: String? = null,
        ) {
            @Serializable
            data class Thumbnail(
                val url: String? = null,
            )
        }
    }
}

object InstantRfc1123Serializer : KSerializer<Instant?> {

    override val descriptor = PrimitiveSerialDescriptor(
        "com.thekeeperofpie.artistalleydatabase.rss.cr.InstantRfc1123Serializer",
        PrimitiveKind.STRING,
    )

    override fun deserialize(decoder: Decoder) = decoder.decodeString().takeIf { it.isNotBlank() }
        ?.let(DateTimeComponents.Formats.RFC_1123::parseOrNull)
        ?.toInstantUsingOffset()

    override fun serialize(encoder: Encoder, value: Instant?) {
        value?.let {
            DateTimeComponents.Formats.RFC_1123.format {
                setDateTimeOffset(
                    value,
                    UtcOffset.ZERO
                )
            }
        }
            .orEmpty()
            .let(encoder::encodeString)
    }
}
