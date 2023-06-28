package com.thekeeperofpie.artistalleydatabase.anime.schedule

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R

enum class AiringScheduleSort(@StringRes val textRes: Int) {

    POPULARITY(R.string.anime_airing_schedule_sort_popularity),
    ID(R.string.anime_airing_schedule_sort_id),
    TIME(R.string.anime_airing_schedule_sort_time),
}
