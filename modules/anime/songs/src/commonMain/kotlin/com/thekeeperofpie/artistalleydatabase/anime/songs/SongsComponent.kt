package com.thekeeperofpie.artistalleydatabase.anime.songs

import com.anilist.data.MediaDetailsQuery
import kotlinx.coroutines.flow.Flow

interface SongsComponent {
    val animeSongsViewModel: (Flow<MediaDetailsQuery.Data.Media?>) -> AnimeSongsViewModel
}
