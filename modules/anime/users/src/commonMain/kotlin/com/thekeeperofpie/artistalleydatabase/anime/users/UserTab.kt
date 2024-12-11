package com.thekeeperofpie.artistalleydatabase.anime.users

import artistalleydatabase.modules.anime.users.generated.resources.Res
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_tab_activity
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_tab_anime_stats
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_tab_manga_stats
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_tab_overview
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_tab_social
import org.jetbrains.compose.resources.StringResource

enum class UserTab(val textRes: StringResource) {
    OVERVIEW(Res.string.anime_user_tab_overview),
    ACTIVITY(Res.string.anime_user_tab_activity),
    ANIME_STATS(Res.string.anime_user_tab_anime_stats),
    MANGA_STATS(Res.string.anime_user_tab_manga_stats),
    SOCIAL(Res.string.anime_user_tab_social),
    ;
}
