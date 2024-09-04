package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.annotation.StringRes
import com.anilist.type.AiringSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption

enum class AiringScheduleSortOption(@StringRes override val textRes: Int): SortOption {

    POPULARITY(R.string.anime_airing_schedule_sort_popularity),
    TIME(R.string.anime_airing_schedule_sort_time),
    EPISODE(R.string.anime_airing_schedule_sort_episode),
    ID(R.string.anime_airing_schedule_sort_id),
    ;

    fun toApiValue(ascending: Boolean) = when(this) {
        POPULARITY -> throw IllegalArgumentException("Airing popularity sort has no API value")
        ID -> if (ascending) AiringSort.ID else AiringSort.ID_DESC
        TIME -> if (ascending) AiringSort.TIME else AiringSort.TIME_DESC
        EPISODE -> if (ascending) AiringSort.EPISODE else AiringSort.EPISODE_DESC
    }
}
