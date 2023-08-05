package com.thekeeperofpie.artistalleydatabase.anime

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.vector.ImageVector

enum class AnimeRootNavDestination(
    val id: String,
    val icon: ImageVector,
    @StringRes val textRes: Int,
    val requiresAuth: Boolean = false,
    val requiresUnlock: Boolean = false,
) {
    HOME(
        "home",
        Icons.Filled.Home,
        R.string.anime_screen_home,
    ),
    ANIME(
        "anime_list",
        Icons.Filled.VideoLibrary,
        R.string.anime_screen_anime,
        requiresAuth = true,
        requiresUnlock = true,
    ),
    MANGA(
        "manga_list",
        Icons.Filled.LibraryBooks,
        R.string.anime_screen_manga,
        requiresAuth = true,
        requiresUnlock = true,
    ),
    SEARCH("anime_search", Icons.Filled.Search, R.string.anime_screen_search),
    PROFILE(
        id = "anime_profile",
        icon = Icons.Filled.Person,
        textRes = R.string.anime_screen_profile,
        requiresUnlock = true,
    ),
    UNLOCK("anime_unlock", Icons.Filled.Lock, R.string.anime_screen_unlock),
    ;

    companion object {
        val StateSaver = object : Saver<AnimeRootNavDestination, String> {
            override fun restore(value: String) =
                AnimeRootNavDestination.values().find { it.id == value } ?: HOME

            override fun SaverScope.save(value: AnimeRootNavDestination) = value.id
        }
    }
}
