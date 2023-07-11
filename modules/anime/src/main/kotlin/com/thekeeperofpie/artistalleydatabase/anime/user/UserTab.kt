package com.thekeeperofpie.artistalleydatabase.anime.user

import com.thekeeperofpie.artistalleydatabase.anime.R

enum class UserTab(
    val textRes: Int
) {
    OVERVIEW(R.string.anime_user_tab_overview),
    ANIME_STATS(R.string.anime_user_tab_anime_stats),
    MANGA_STATS(R.string.anime_user_tab_manga_stats),
    SOCIAL(R.string.anime_user_tab_social),
    ;
}
