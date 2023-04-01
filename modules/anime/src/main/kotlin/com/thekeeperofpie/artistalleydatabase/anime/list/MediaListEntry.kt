package com.thekeeperofpie.artistalleydatabase.anime.list

import com.anilist.UserMediaListQuery
import com.anilist.type.MediaListStatus

sealed interface MediaListEntry {

    data class Header(val name: String, val status: MediaListStatus?) : MediaListEntry

    data class Item(
        val entry: UserMediaListQuery.Data.MediaListCollection.List.Entry,
    ) : MediaListEntry

    data class LoadMore(val id: String) : MediaListEntry
}