package com.thekeeperofpie.artistalleydatabase.anime.songs

import com.anilist.data.MediaDetailsQuery

interface AnimeSongsProvider {

    suspend fun getSongs(media: MediaDetailsQuery.Data.Media): List<AnimeSongEntry>
}
