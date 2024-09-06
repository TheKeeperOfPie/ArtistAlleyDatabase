package com.thekeeperofpie.artistalleydatabase.anime.schedule

import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_airing_schedule_sort_episode
import artistalleydatabase.modules.anime.generated.resources.anime_airing_schedule_sort_id
import artistalleydatabase.modules.anime.generated.resources.anime_airing_schedule_sort_popularity
import artistalleydatabase.modules.anime.generated.resources.anime_airing_schedule_sort_time
import com.anilist.type.AiringSort
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class AiringScheduleSortOption(override val textRes: StringResource): SortOption {

    POPULARITY(Res.string.anime_airing_schedule_sort_popularity),
    TIME(Res.string.anime_airing_schedule_sort_time),
    EPISODE(Res.string.anime_airing_schedule_sort_episode),
    ID(Res.string.anime_airing_schedule_sort_id),
    ;

    fun toApiValue(ascending: Boolean) = when(this) {
        POPULARITY -> throw IllegalArgumentException("Airing popularity sort has no API value")
        ID -> if (ascending) AiringSort.ID else AiringSort.ID_DESC
        TIME -> if (ascending) AiringSort.TIME else AiringSort.TIME_DESC
        EPISODE -> if (ascending) AiringSort.EPISODE else AiringSort.EPISODE_DESC
    }
}
