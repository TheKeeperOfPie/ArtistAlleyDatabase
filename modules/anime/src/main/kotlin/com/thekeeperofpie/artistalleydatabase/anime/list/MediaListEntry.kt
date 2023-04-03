package com.thekeeperofpie.artistalleydatabase.anime.list

import com.anilist.UserMediaListQuery
import com.anilist.type.MediaListStatus

sealed interface MediaListEntry {

    data class Header(val name: String, val status: MediaListStatus?) : MediaListEntry

    data class Item(
        val media: UserMediaListQuery.Data.MediaListCollection.List.Entry.Media,
    ) : MediaListEntry

    data class LoadMore(val id: String) : MediaListEntry
}