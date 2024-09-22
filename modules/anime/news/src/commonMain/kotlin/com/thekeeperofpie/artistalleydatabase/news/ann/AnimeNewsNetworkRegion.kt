package com.thekeeperofpie.artistalleydatabase.news.ann

import artistalleydatabase.modules.anime.news.generated.resources.Res
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_network_region_australia_new_zealand
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_network_region_francais
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_network_region_india
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_network_region_southeast_asia
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_network_region_usa_canada
import artistalleydatabase.modules.anime.news.generated.resources.anime_news_network_region_world
import org.jetbrains.compose.resources.StringResource

enum class AnimeNewsNetworkRegion(val id: String, val textRes: StringResource) {
    WORLD("w", Res.string.anime_news_network_region_world),
    USA_CANADA("us", Res.string.anime_news_network_region_usa_canada),
    AUSTRALIA_NEW_ZEALAND("au", Res.string.anime_news_network_region_australia_new_zealand),
    INDIA("in", Res.string.anime_news_network_region_india),
    SOUTHEAST_ASIA("sea", Res.string.anime_news_network_region_southeast_asia),
    FRANCAIS("fr", Res.string.anime_news_network_region_francais),
    ;
}
