package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.annotation.StringRes
import com.anilist.type.MediaSort
import com.thekeeperofpie.artistalleydatabase.anime.R

enum class MediaSortOption(@StringRes override val textRes: Int) :
    AnimeMediaFilterController.Data.SortOption {

    // Omissions: CHAPTERS/VOLUMES are only applicable for manga, and SEARCH_MATCH is used as a default
    DEFAULT(R.string.anime_media_list_sort_default),
    ID(R.string.anime_media_sort_id),
    TITLE_ROMAJI(R.string.anime_media_sort_title_romaji),
    TITLE_ENGLISH(R.string.anime_media_sort_title_english),
    TITLE_NATIVE(R.string.anime_media_sort_title_native),
    TYPE(R.string.anime_media_sort_type),
    FORMAT(R.string.anime_media_sort_format),
    START_DATE(R.string.anime_media_sort_start_date),
    END_DATE(R.string.anime_media_sort_end_date),
    SCORE(R.string.anime_media_sort_score),
    POPULARITY(R.string.anime_media_sort_popularity),
    TRENDING(R.string.anime_media_sort_trending),
    EPISODES(R.string.anime_media_sort_episodes),
    DURATION(R.string.anime_media_sort_duration),
    STATUS(R.string.anime_media_sort_status),
    UPDATED_AT(R.string.anime_media_sort_updated_at),
    FAVOURITES(R.string.anime_media_sort_favourites),

    ;

    fun toApiValue(ascending: Boolean) = when (this) {
        DEFAULT -> MediaSort.SEARCH_MATCH
        ID -> if (ascending) MediaSort.ID else MediaSort.ID_DESC
        TITLE_ROMAJI -> if (ascending) MediaSort.TITLE_ROMAJI else MediaSort.TITLE_ROMAJI_DESC
        TITLE_ENGLISH -> if (ascending) MediaSort.TITLE_ENGLISH else MediaSort.TITLE_ENGLISH_DESC
        TITLE_NATIVE -> if (ascending) MediaSort.TITLE_NATIVE else MediaSort.TITLE_NATIVE_DESC
        TYPE -> if (ascending) MediaSort.TYPE else MediaSort.TYPE_DESC
        FORMAT -> if (ascending) MediaSort.FORMAT else MediaSort.FORMAT_DESC
        START_DATE -> if (ascending) MediaSort.START_DATE else MediaSort.START_DATE_DESC
        END_DATE -> if (ascending) MediaSort.END_DATE else MediaSort.END_DATE_DESC
        SCORE -> if (ascending) MediaSort.SCORE else MediaSort.SCORE_DESC
        POPULARITY -> if (ascending) MediaSort.POPULARITY else MediaSort.POPULARITY_DESC
        TRENDING -> if (ascending) MediaSort.TRENDING else MediaSort.TRENDING_DESC
        EPISODES -> if (ascending) MediaSort.EPISODES else MediaSort.EPISODES_DESC
        DURATION -> if (ascending) MediaSort.DURATION else MediaSort.DURATION_DESC
        STATUS -> if (ascending) MediaSort.STATUS else MediaSort.STATUS_DESC
        UPDATED_AT -> if (ascending) MediaSort.UPDATED_AT else MediaSort.UPDATED_AT_DESC
        FAVOURITES -> if (ascending) MediaSort.FAVOURITES else MediaSort.FAVOURITES_DESC
    }
}