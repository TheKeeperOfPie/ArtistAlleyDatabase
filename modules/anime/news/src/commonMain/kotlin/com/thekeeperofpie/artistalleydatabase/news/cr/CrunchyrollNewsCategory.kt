package com.thekeeperofpie.artistalleydatabase.news.cr

import artistalleydatabase.modules.anime.news.generated.resources.Res
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_crunchyroll_category_announcements
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_crunchyroll_category_features
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_crunchyroll_category_guides
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_crunchyroll_category_interviews
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_crunchyroll_category_news
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_crunchyroll_category_quizzes
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_network_category_unknown
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.compose.resources.StringResource

enum class CrunchyrollNewsCategory(val id: String, val textRes: StringResource) {
    NEWS("News", Res.string.anime_news_crunchyroll_category_news),
    ANNOUNCEMENTS("Announcements", Res.string.anime_news_crunchyroll_category_announcements),
    FEATURES("Features", Res.string.anime_news_crunchyroll_category_features),
    GUIDES("Guides", Res.string.anime_news_crunchyroll_category_guides),
    INTERVIEWS("Interviews", Res.string.anime_news_crunchyroll_category_interviews),
    QUIZZES("Quizzes", Res.string.anime_news_crunchyroll_category_quizzes),
    UNKNOWN("Unknown", Res.string.anime_news_network_category_unknown),
    ;

    object Serializer : KSerializer<CrunchyrollNewsCategory> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
            CrunchyrollNewsCategory::class.qualifiedName!!,
            PrimitiveKind.STRING
        )

        override fun deserialize(decoder: Decoder): CrunchyrollNewsCategory {
            val value = decoder.decodeString()
            return CrunchyrollNewsCategory.entries
                .find { it.id.equals(value, ignoreCase = true) }
                ?: UNKNOWN
        }

        override fun serialize(encoder: Encoder, value: CrunchyrollNewsCategory) =
            encoder.encodeString(value.id)
    }
}
