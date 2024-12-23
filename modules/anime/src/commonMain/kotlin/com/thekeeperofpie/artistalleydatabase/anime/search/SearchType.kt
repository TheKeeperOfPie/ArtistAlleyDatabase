package com.thekeeperofpie.artistalleydatabase.anime.search

import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_search_anime
import artistalleydatabase.modules.anime.generated.resources.anime_search_characters
import artistalleydatabase.modules.anime.generated.resources.anime_search_manga
import artistalleydatabase.modules.anime.generated.resources.anime_search_staff
import artistalleydatabase.modules.anime.generated.resources.anime_search_studio
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_anime
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_character
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_manga
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_staff
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_studio
import artistalleydatabase.modules.anime.generated.resources.anime_search_type_user
import artistalleydatabase.modules.anime.generated.resources.anime_search_user
import org.jetbrains.compose.resources.StringResource

enum class SearchType(val tabText: StringResource, val searchLabel: StringResource) {
    ANIME(Res.string.anime_search_type_anime, Res.string.anime_search_anime),
    MANGA(Res.string.anime_search_type_manga, Res.string.anime_search_manga),
    CHARACTER(Res.string.anime_search_type_character, Res.string.anime_search_characters),
    STAFF(Res.string.anime_search_type_staff, Res.string.anime_search_staff),
    STUDIO(Res.string.anime_search_type_studio, Res.string.anime_search_studio),
    USER(Res.string.anime_search_type_user, Res.string.anime_search_user),
}
