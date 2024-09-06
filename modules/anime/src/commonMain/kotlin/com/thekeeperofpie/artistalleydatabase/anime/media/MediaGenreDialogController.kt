package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class MediaGenreDialogController {
    var genreShown by mutableStateOf<MediaGenre?>(null)

    fun onLongClickGenre(genre: String) {
        genreShown = MediaGenre.values().find { it.id == genre }
    }
}

val LocalMediaGenreDialogController = staticCompositionLocalOf { MediaGenreDialogController() }
