package com.thekeeperofpie.artistalleydatabase.anime.list

import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_average_score
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_end_date
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_my_added_time
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_my_finished_on
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_my_started_on
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_my_updated_time
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_priority
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_progress
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_start_date
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_status
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_their_added_time
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_their_finished_on
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_their_started_on
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_their_updated_time
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_title_english
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_title_my_score
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_title_native
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_title_popularity
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_title_romaji
import artistalleydatabase.modules.anime.generated.resources.anime_media_list_sort_title_their_score
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class MediaListSortOption(
    override val textRes: StringResource,
    val forDifferentUser: Boolean? = null,
) : SortOption {

    AVERAGE_SCORE(Res.string.anime_media_list_sort_average_score),
    STATUS(Res.string.anime_media_list_sort_status),
    PROGRESS(Res.string.anime_media_list_sort_progress),
    PRIORITY(Res.string.anime_media_list_sort_priority),
    START_DATE(Res.string.anime_media_list_sort_start_date),
    END_DATE(Res.string.anime_media_list_sort_end_date),
    MY_STARTED_ON(Res.string.anime_media_list_sort_my_started_on, forDifferentUser = false),
    MY_FINISHED_ON(Res.string.anime_media_list_sort_my_finished_on, forDifferentUser = false),
    THEIR_STARTED_ON(Res.string.anime_media_list_sort_their_started_on, forDifferentUser = true),
    THEIR_FINISHED_ON(Res.string.anime_media_list_sort_their_finished_on, forDifferentUser = true),
    MY_ADDED_TIME(Res.string.anime_media_list_sort_my_added_time, forDifferentUser = false),
    MY_UPDATED_TIME(Res.string.anime_media_list_sort_my_updated_time, forDifferentUser = false),
    THEIR_ADDED_TIME(Res.string.anime_media_list_sort_their_added_time, forDifferentUser = true),
    THEIR_UPDATED_TIME(Res.string.anime_media_list_sort_their_updated_time, forDifferentUser = true),
    TITLE_ROMAJI(Res.string.anime_media_list_sort_title_romaji),
    TITLE_ENGLISH(Res.string.anime_media_list_sort_title_english),
    TITLE_NATIVE(Res.string.anime_media_list_sort_title_native),
    POPULARITY(Res.string.anime_media_list_sort_title_popularity),
    MY_SCORE(Res.string.anime_media_list_sort_title_my_score, forDifferentUser = false),
    THEIR_SCORE(Res.string.anime_media_list_sort_title_their_score, forDifferentUser = true),

    ;
}
