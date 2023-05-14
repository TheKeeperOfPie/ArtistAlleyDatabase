package com.thekeeperofpie.artistalleydatabase.anime

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

enum class AnimeNavDestinations(
    val id: String,
    val icon: ImageVector,
    @StringRes val textRes: Int,
) {

    ANIME("anime_list", Icons.Filled.VideoLibrary, R.string.anime_screen_anime),
    MANGA("manga_list", Icons.Filled.LibraryBooks, R.string.anime_screen_manga),
    SEARCH("anime_search", Icons.Filled.Search, R.string.anime_screen_search),
    PROFILE("anime_profile", Icons.Filled.Person, R.string.anime_screen_profile),

    ;

    companion object {
        // TODO: Make this configurable
        const val DEFAULT_INDEX = 1
    }
}
