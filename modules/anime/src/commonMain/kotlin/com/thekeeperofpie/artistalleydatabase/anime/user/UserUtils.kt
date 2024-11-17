package com.thekeeperofpie.artistalleydatabase.anime.user

import com.anilist.data.fragment.UserWithFavorites
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry

object UserUtils {

    fun buildInitialMediaEntries(user: UserWithFavorites): List<MediaWithListStatusEntry> {
        val anime = user.favourites?.anime?.edges
            ?.filterNotNull()
            ?.sortedBy { it.favouriteOrder }
            ?.mapNotNull { it.node }
            .orEmpty()
            .map { MediaWithListStatusEntry(media = it) }

        val manga = user.favourites?.manga?.edges
            ?.filterNotNull()
            ?.sortedBy { it.favouriteOrder }
            ?.mapNotNull { it.node }
            .orEmpty()
            .map { MediaWithListStatusEntry(media = it) }
        return (anime + manga).distinctBy { it.media.id }
    }
}
