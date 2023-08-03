package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.annotation.StringRes
import com.anilist.type.UserSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class UserSortOption(
    @StringRes override val textRes: Int,
    override val supportsAscending: Boolean = true,
) : SortOption {

    SEARCH_MATCH(R.string.anime_user_sort_search_match, supportsAscending = false),
    ID(R.string.anime_user_sort_id),
    USERNAME(R.string.anime_user_sort_username),
    WATCHED_TIME(R.string.anime_user_sort_watched_time),
    CHAPTERS_READ(R.string.anime_user_sort_chapters_read),

    ;

    fun toApiValue(ascending: Boolean) = when (this) {
        SEARCH_MATCH -> listOf(UserSort.SEARCH_MATCH, UserSort.ID_DESC)
        ID -> listOf(if (ascending) UserSort.ID else UserSort.ID_DESC)
        USERNAME -> listOf(
            if (ascending) UserSort.USERNAME else UserSort.USERNAME_DESC,
            UserSort.SEARCH_MATCH,
        )
        WATCHED_TIME -> listOf(
            if (ascending) UserSort.WATCHED_TIME else UserSort.WATCHED_TIME_DESC,
            UserSort.SEARCH_MATCH,
        )
        CHAPTERS_READ -> listOf(
            if (ascending) UserSort.CHAPTERS_READ else UserSort.CHAPTERS_READ_DESC,
            UserSort.SEARCH_MATCH,
        )
    }
}
