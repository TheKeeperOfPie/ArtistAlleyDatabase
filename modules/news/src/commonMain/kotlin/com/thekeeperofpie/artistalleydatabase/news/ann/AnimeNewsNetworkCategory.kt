package com.thekeeperofpie.artistalleydatabase.news.ann

import artistalleydatabase.modules.news.generated.resources.Res
import artistalleydatabase.modules.news.generated.resources.anime_news_network_category_anime
import artistalleydatabase.modules.news.generated.resources.anime_news_network_category_games
import artistalleydatabase.modules.news.generated.resources.anime_news_network_category_industry
import artistalleydatabase.modules.news.generated.resources.anime_news_network_category_live_action
import artistalleydatabase.modules.news.generated.resources.anime_news_network_category_manga
import artistalleydatabase.modules.news.generated.resources.anime_news_network_category_music
import artistalleydatabase.modules.news.generated.resources.anime_news_network_category_novels
import artistalleydatabase.modules.news.generated.resources.anime_news_network_category_people
import artistalleydatabase.modules.news.generated.resources.anime_news_network_category_unknown
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.compose.resources.StringResource

enum class AnimeNewsNetworkCategory(val id: String, val textRes: StringResource) {
    ANIME("Anime", Res.string.anime_news_network_category_anime),
    MANGA("Manga", Res.string.anime_news_network_category_manga),
    LIVE_ACTION("Live-Action", Res.string.anime_news_network_category_live_action),
    GAMES("Games", Res.string.anime_news_network_category_games),
    NOVELS("Novels", Res.string.anime_news_network_category_novels),
    MUSIC("Music", Res.string.anime_news_network_category_music),
    PEOPLE("People", Res.string.anime_news_network_category_people),
    INDUSTRY("Industry", Res.string.anime_news_network_category_industry),
    UNKNOWN("Unknown", Res.string.anime_news_network_category_unknown),
    ;

    object Serializer : KSerializer<AnimeNewsNetworkCategory> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            AnimeNewsNetworkCategory::class.qualifiedName!!,
            PrimitiveKind.STRING
        )

        override fun deserialize(decoder: Decoder): AnimeNewsNetworkCategory {
            val value = decoder.decodeString()
            return AnimeNewsNetworkCategory.entries
                .find { it.id.equals(value, ignoreCase = true) }
                ?: UNKNOWN
        }

        override fun serialize(encoder: Encoder, value: AnimeNewsNetworkCategory) =
            encoder.encodeString(value.id)
    }
}

