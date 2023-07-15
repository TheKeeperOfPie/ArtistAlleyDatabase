package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class MediaListSortOption(@StringRes override val textRes: Int) : SortOption {

    SCORE(R.string.anime_media_list_sort_score),
    STATUS(R.string.anime_media_list_sort_status),
    PROGRESS(R.string.anime_media_list_sort_progress),
    PRIORITY(R.string.anime_media_list_sort_priority),
    STARTED_ON(R.string.anime_media_list_sort_started_on),
    FINISHED_ON(R.string.anime_media_list_sort_finished_on),
    ADDED_TIME(R.string.anime_media_list_sort_added_time),
    UPDATED_TIME(R.string.anime_media_list_sort_updated_time),
    TITLE_ROMAJI(R.string.anime_media_list_sort_title_romaji),
    TITLE_ENGLISH(R.string.anime_media_list_sort_title_english),
    TITLE_NATIVE(R.string.anime_media_list_sort_title_native),
    POPULARITY(R.string.anime_media_list_sort_title_popularity),

    ;
}
