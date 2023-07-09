package com.thekeeperofpie.artistalleydatabase.anime.utils

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R

enum class AnimeNewsNetworkRegion(val id: String, @StringRes val nameTextRes: Int) {
    WORLD("w", R.string.anime_news_network_region_world),
    USA_CANADA("us", R.string.anime_news_network_region_usa_canada),
    AUSTRALIA_NEW_ZEALAND("au", R.string.anime_news_network_region_australia_new_zealand),
    INDIA("in", R.string.anime_news_network_region_india),
    SOUTHEAST_ASIA("sea", R.string.anime_news_network_region_southeast_asia),
    FRANCAIS("fr", R.string.anime_news_network_region_francais),
    ;
}
