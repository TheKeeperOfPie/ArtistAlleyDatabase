package com.thekeeperofpie.artistalleydatabase.anime.user.stats

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R

enum class UserStatsTab(@StringRes val textRes: Int) {
    STATS(R.string.anime_user_stats_tab_stats),
    GENRES(R.string.anime_user_stats_tab_genres),
    ;
}
