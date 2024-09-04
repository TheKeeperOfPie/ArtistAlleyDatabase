package com.thekeeperofpie.artistalleydatabase.anime.media.filter

import androidx.annotation.StringRes
import com.anilist.type.MediaSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption

enum class MediaSortOption(
    @StringRes override val textRes: Int,
    override val supportsAscending: Boolean = true,
) : SortOption {

    SEARCH_MATCH(R.string.anime_media_sort_search_match, false),
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
    VOLUMES(R.string.anime_media_sort_volumes),
    CHAPTERS(R.string.anime_media_sort_chapters),
    DURATION(R.string.anime_media_sort_duration),
    STATUS(R.string.anime_media_sort_status),
    UPDATED_AT(R.string.anime_media_sort_updated_at),
    FAVOURITES(R.string.anime_media_sort_favourites),

    ;

    fun toApiValue(ascending: Boolean) = when (this) {
        ID -> listOf(if (ascending) MediaSort.ID else MediaSort.ID_DESC)
        SEARCH_MATCH -> listOf(MediaSort.SEARCH_MATCH, MediaSort.TRENDING_DESC)
        TITLE_ROMAJI -> listOf(if (ascending) MediaSort.TITLE_ROMAJI else MediaSort.TITLE_ROMAJI_DESC)
        TITLE_ENGLISH -> listOf(if (ascending) MediaSort.TITLE_ENGLISH else MediaSort.TITLE_ENGLISH_DESC)
        TITLE_NATIVE -> listOf(if (ascending) MediaSort.TITLE_NATIVE else MediaSort.TITLE_NATIVE_DESC)
        TYPE -> listOf(if (ascending) MediaSort.TYPE else MediaSort.TYPE_DESC)
        FORMAT -> listOf(if (ascending) MediaSort.FORMAT else MediaSort.FORMAT_DESC)
        START_DATE -> listOf(if (ascending) MediaSort.START_DATE else MediaSort.START_DATE_DESC)
        END_DATE -> listOf(if (ascending) MediaSort.END_DATE else MediaSort.END_DATE_DESC)
        SCORE -> listOf(if (ascending) MediaSort.SCORE else MediaSort.SCORE_DESC)
        POPULARITY -> listOf(if (ascending) MediaSort.POPULARITY else MediaSort.POPULARITY_DESC)
        TRENDING -> listOf(if (ascending) MediaSort.TRENDING else MediaSort.TRENDING_DESC)
        EPISODES -> listOf(if (ascending) MediaSort.EPISODES else MediaSort.EPISODES_DESC)
        VOLUMES -> listOf(if (ascending) MediaSort.VOLUMES else MediaSort.VOLUMES_DESC)
        CHAPTERS -> listOf(if (ascending) MediaSort.CHAPTERS else MediaSort.CHAPTERS_DESC)
        DURATION -> listOf(if (ascending) MediaSort.DURATION else MediaSort.DURATION_DESC)
        STATUS -> listOf(if (ascending) MediaSort.STATUS else MediaSort.STATUS_DESC)
        UPDATED_AT -> listOf(if (ascending) MediaSort.UPDATED_AT else MediaSort.UPDATED_AT_DESC)
        FAVOURITES -> listOf(if (ascending) MediaSort.FAVOURITES else MediaSort.FAVOURITES_DESC)
    }
}
