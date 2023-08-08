package com.thekeeperofpie.artistalleydatabase.anime.forum

import com.anilist.type.ThreadSort
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.filter.SortOption

enum class ForumThreadSortOption(
    override val textRes: Int,
    override val supportsAscending: Boolean = true,
) : SortOption {
    // IS_STICKY and ID omitted
    SEARCH_MATCH(R.string.anime_forum_thread_sort_search_match, supportsAscending = false),
    TITLE(R.string.anime_forum_thread_sort_title),
    CREATED_AT(R.string.anime_forum_thread_sort_created_at),
    UPDATED_AT(R.string.anime_forum_thread_sort_updated_at),
    VIEW_COUNT(R.string.anime_forum_thread_sort_view_count),
    REPLIED_AT(R.string.anime_forum_thread_sort_replied_at),
    REPLY_COUNT(R.string.anime_forum_thread_sort_reply_count),
    ;

    fun toApiValue(sortAscending: Boolean) = when (this) {
        SEARCH_MATCH -> listOf(ThreadSort.SEARCH_MATCH, ThreadSort.REPLIED_AT_DESC)
        TITLE -> listOf(
            if (sortAscending) ThreadSort.TITLE else ThreadSort.TITLE_DESC,
            ThreadSort.REPLIED_AT
        )
        CREATED_AT -> listOf(if (sortAscending) ThreadSort.CREATED_AT else ThreadSort.CREATED_AT_DESC)
        UPDATED_AT -> listOf(if (sortAscending) ThreadSort.UPDATED_AT else ThreadSort.UPDATED_AT_DESC)
        VIEW_COUNT -> listOf(if (sortAscending) ThreadSort.VIEW_COUNT else ThreadSort.VIEW_COUNT_DESC)
        REPLIED_AT -> listOf(if (sortAscending) ThreadSort.REPLIED_AT else ThreadSort.REPLIED_AT_DESC)
        REPLY_COUNT -> listOf(if (sortAscending) ThreadSort.REPLY_COUNT else ThreadSort.REPLY_COUNT_DESC)
    }
}
