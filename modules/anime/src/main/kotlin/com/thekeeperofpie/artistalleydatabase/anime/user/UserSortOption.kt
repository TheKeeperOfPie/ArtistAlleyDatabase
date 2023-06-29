package com.thekeeperofpie.artistalleydatabase.anime.user

import androidx.annotation.StringRes
import com.anilist.type.UserSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class UserSortOption(@StringRes override val textRes: Int) : SortOption {

    // Omissions: SEARCH_MATCH is used as a default, RELEVANCE not useful for search
    ID(R.string.anime_user_sort_id),
    USERNAME(R.string.anime_user_sort_username),
    WATCHED_TIME(R.string.anime_user_sort_watched_time),
    CHAPTERS_READ(R.string.anime_user_sort_chapters_read),

    ;

    fun toApiValue(ascending: Boolean) = when (this) {
        ID -> if (ascending) UserSort.ID else UserSort.ID_DESC
        USERNAME -> if (ascending) UserSort.USERNAME else UserSort.USERNAME_DESC
        WATCHED_TIME -> if (ascending) UserSort.WATCHED_TIME else UserSort.WATCHED_TIME_DESC
        CHAPTERS_READ -> if (ascending) UserSort.CHAPTERS_READ else UserSort.CHAPTERS_READ_DESC
    }
}
