package com.thekeeperofpie.artistalleydatabase.anime.user.stats

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R

enum class UserStatsTab(@StringRes val textRes: Int, val isAnimeOnly: Boolean = false) {
    STATS(R.string.anime_user_stats_tab_stats),
    GENRES(R.string.anime_user_stats_tab_genres),
    TAGS(R.string.anime_user_stats_tab_tags),
    VOICE_ACTORS(R.string.anime_user_stats_tab_voice_actors, isAnimeOnly = true),
    STUDIOS(R.string.anime_user_stats_tab_studios, isAnimeOnly = true),
    STAFF(R.string.anime_user_stats_tab_staff),
    ;
}
