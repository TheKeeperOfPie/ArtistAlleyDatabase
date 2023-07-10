package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R

enum class AnimeNewsNetworkCategory(val id: String, @StringRes val textRes: Int) {
    ANIME("Anime", R.string.anime_news_network_category_anime),
    MANGA("Manga", R.string.anime_news_network_category_manga),
    LIVE_ACTION("Live-Action", R.string.anime_news_network_category_live_action),
    GAMES("Games", R.string.anime_news_network_category_games),
    NOVELS("Novels", R.string.anime_news_network_category_novels),
    MUSIC("Music", R.string.anime_news_network_category_music),
    PEOPLE("People", R.string.anime_news_network_category_people),
    UNKNOWN("Unknown", R.string.anime_news_network_category_unknown),
}
