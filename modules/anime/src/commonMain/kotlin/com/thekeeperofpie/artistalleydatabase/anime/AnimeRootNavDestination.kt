package com.thekeeperofpie.artistalleydatabase.anime

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.vector.ImageVector
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_screen_anime
import artistalleydatabase.modules.anime.generated.resources.anime_screen_home
import artistalleydatabase.modules.anime.generated.resources.anime_screen_manga
import artistalleydatabase.modules.anime.generated.resources.anime_screen_profile
import artistalleydatabase.modules.anime.generated.resources.anime_screen_search
import artistalleydatabase.modules.anime.generated.resources.anime_screen_unlock
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.LibraryBooks
import com.thekeeperofpie.artistalleydatabase.icons.filled.Home
import com.thekeeperofpie.artistalleydatabase.icons.filled.Lock
import com.thekeeperofpie.artistalleydatabase.icons.filled.Person
import com.thekeeperofpie.artistalleydatabase.icons.filled.Search
import com.thekeeperofpie.artistalleydatabase.icons.filled.VideoLibrary
import org.jetbrains.compose.resources.StringResource

enum class AnimeRootNavDestination(
    val id: String,
    val icon: ImageVector,
    val textRes: StringResource,
    val requiresAuth: Boolean = false,
    val requiresUnlock: Boolean = false,
) {
    HOME(
        "home",
        Icons.Filled.Home,
        Res.string.anime_screen_home,
    ),
    ANIME(
        "anime_list",
        Icons.Filled.VideoLibrary,
        Res.string.anime_screen_anime,
        requiresAuth = true,
        requiresUnlock = true,
    ),
    MANGA(
        "manga_list",
        Icons.AutoMirrored.Filled.LibraryBooks,
        Res.string.anime_screen_manga,
        requiresAuth = true,
        requiresUnlock = true,
    ),
    SEARCH("anime_search", Icons.Filled.Search, Res.string.anime_screen_search),
    PROFILE(
        id = "anime_profile",
        icon = Icons.Filled.Person,
        textRes = Res.string.anime_screen_profile,
        requiresUnlock = true,
    ),
    UNLOCK("anime_unlock", Icons.Filled.Lock, Res.string.anime_screen_unlock),
    ;

    companion object {
        val StateSaver = object : Saver<AnimeRootNavDestination, String> {
            override fun restore(value: String) = entries.find { it.id == value } ?: HOME

            override fun SaverScope.save(value: AnimeRootNavDestination) = value.id
        }
    }
}
