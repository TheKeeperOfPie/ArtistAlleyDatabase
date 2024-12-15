package com.thekeeperofpie.artistalleydatabase.anime.users.stats

import artistalleydatabase.modules.anime.users.generated.resources.Res
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_stats_tab_genres
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_stats_tab_staff
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_stats_tab_stats
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_stats_tab_studios
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_stats_tab_tags
import artistalleydatabase.modules.anime.users.generated.resources.anime_user_stats_tab_voice_actors
import org.jetbrains.compose.resources.StringResource

enum class UserStatsTab(val textRes: StringResource, val isAnimeOnly: Boolean = false) {
    STATS(Res.string.anime_user_stats_tab_stats),
    GENRES(Res.string.anime_user_stats_tab_genres),
    TAGS(Res.string.anime_user_stats_tab_tags),
    VOICE_ACTORS(Res.string.anime_user_stats_tab_voice_actors, isAnimeOnly = true),
    STUDIOS(Res.string.anime_user_stats_tab_studios, isAnimeOnly = true),
    STAFF(Res.string.anime_user_stats_tab_staff),
    ;
}
