package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class MediaListSortOption(
    @StringRes override val textRes: Int,
    val forDifferentUser: Boolean? = null,
) : SortOption {

    AVERAGE_SCORE(R.string.anime_media_list_sort_average_score),
    STATUS(R.string.anime_media_list_sort_status),
    PROGRESS(R.string.anime_media_list_sort_progress),
    PRIORITY(R.string.anime_media_list_sort_priority),
    MY_STARTED_ON(R.string.anime_media_list_sort_my_started_on, forDifferentUser = false),
    MY_FINISHED_ON(R.string.anime_media_list_sort_my_finished_on, forDifferentUser = false),
    THEIR_STARTED_ON(R.string.anime_media_list_sort_their_started_on, forDifferentUser = true),
    THEIR_FINISHED_ON(R.string.anime_media_list_sort_their_finished_on, forDifferentUser = true),
    MY_ADDED_TIME(R.string.anime_media_list_sort_my_added_time, forDifferentUser = false),
    MY_UPDATED_TIME(R.string.anime_media_list_sort_my_updated_time, forDifferentUser = false),
    THEIR_ADDED_TIME(R.string.anime_media_list_sort_their_added_time, forDifferentUser = true),
    THEIR_UPDATED_TIME(R.string.anime_media_list_sort_their_updated_time, forDifferentUser = true),
    TITLE_ROMAJI(R.string.anime_media_list_sort_title_romaji),
    TITLE_ENGLISH(R.string.anime_media_list_sort_title_english),
    TITLE_NATIVE(R.string.anime_media_list_sort_title_native),
    POPULARITY(R.string.anime_media_list_sort_title_popularity),
    MY_SCORE(R.string.anime_media_list_sort_title_my_score, forDifferentUser = false),
    THEIR_SCORE(R.string.anime_media_list_sort_title_their_score, forDifferentUser = true),

    ;
}
