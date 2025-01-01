package com.thekeeperofpie.artistalleydatabase.anime.users.data.filter

import artistalleydatabase.modules.anime.users.data.generated.resources.Res
import artistalleydatabase.modules.anime.users.data.generated.resources.anime_user_sort_chapters_read
import artistalleydatabase.modules.anime.users.data.generated.resources.anime_user_sort_id
import artistalleydatabase.modules.anime.users.data.generated.resources.anime_user_sort_search_match
import artistalleydatabase.modules.anime.users.data.generated.resources.anime_user_sort_username
import artistalleydatabase.modules.anime.users.data.generated.resources.anime_user_sort_watched_time
import com.anilist.data.type.UserSort
import com.thekeeperofpie.artistalleydatabase.utils_compose.filter.SortOption
import org.jetbrains.compose.resources.StringResource

enum class UserSortOption(
    override val textRes: StringResource,
    override val supportsAscending: Boolean = true,
) : SortOption {

    SEARCH_MATCH(Res.string.anime_user_sort_search_match, supportsAscending = false),
    ID(Res.string.anime_user_sort_id),
    USERNAME(Res.string.anime_user_sort_username),
    WATCHED_TIME(Res.string.anime_user_sort_watched_time),
    CHAPTERS_READ(Res.string.anime_user_sort_chapters_read),
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
