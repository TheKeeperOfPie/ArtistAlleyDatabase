package com.thekeeperofpie.artistalleydatabase.anime.users

import com.anilist.data.fragment.MediaWithListStatus
import com.anilist.data.fragment.UserWithFavorites
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider

object UserUtils {

    fun <MediaEntry> buildInitialMediaEntries(
        user: UserWithFavorites,
        mediaEntryProvider: MediaEntryProvider<MediaWithListStatus, MediaEntry>,
    ): List<MediaEntry> {
        val anime = user.favourites?.anime?.edges
            ?.filterNotNull()
            ?.sortedBy { it.favouriteOrder }
            ?.mapNotNull { it.node }
            .orEmpty()
            .map(mediaEntryProvider::mediaEntry)

        val manga = user.favourites?.manga?.edges
            ?.filterNotNull()
            ?.sortedBy { it.favouriteOrder }
            ?.mapNotNull { it.node }
            .orEmpty()
            .map(mediaEntryProvider::mediaEntry)
        return (anime + manga).distinctBy(mediaEntryProvider::id)
    }
}
