package com.thekeeperofpie.artistalleydatabase.anime.list

import androidx.annotation.StringRes
import com.anilist.type.MediaListSort
import com.thekeeperofpie.artistalleydatabase.anime.R

enum class MediaListSortOption(@StringRes val textRes: Int) {

    DEFAULT(R.string.anime_media_list_sort_default),
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

    fun toApiValue(ascending: Boolean) = when (this) {
        DEFAULT -> null
        SCORE -> if (ascending) MediaListSort.SCORE else MediaListSort.SCORE_DESC
        STATUS -> if (ascending) MediaListSort.STATUS else MediaListSort.STATUS_DESC
        PROGRESS -> if (ascending) MediaListSort.PROGRESS else MediaListSort.PROGRESS_DESC
        PRIORITY -> if (ascending) MediaListSort.PRIORITY else MediaListSort.PRIORITY_DESC
        STARTED_ON -> if (ascending) MediaListSort.STARTED_ON else MediaListSort.STARTED_ON_DESC
        FINISHED_ON -> if (ascending) MediaListSort.FINISHED_ON else MediaListSort.FINISHED_ON_DESC
        ADDED_TIME -> if (ascending) MediaListSort.ADDED_TIME else MediaListSort.ADDED_TIME_DESC
        UPDATED_TIME -> if (ascending) MediaListSort.UPDATED_TIME else MediaListSort.UPDATED_TIME_DESC
        TITLE_ROMAJI -> if (ascending) MediaListSort.MEDIA_TITLE_ROMAJI else MediaListSort.MEDIA_TITLE_ROMAJI_DESC
        TITLE_ENGLISH -> if (ascending) MediaListSort.MEDIA_TITLE_ENGLISH else MediaListSort.MEDIA_TITLE_ENGLISH_DESC
        TITLE_NATIVE -> if (ascending) MediaListSort.MEDIA_TITLE_NATIVE else MediaListSort.MEDIA_TITLE_NATIVE_DESC
        POPULARITY -> if (ascending) MediaListSort.MEDIA_POPULARITY else MediaListSort.MEDIA_POPULARITY_DESC
    }
}