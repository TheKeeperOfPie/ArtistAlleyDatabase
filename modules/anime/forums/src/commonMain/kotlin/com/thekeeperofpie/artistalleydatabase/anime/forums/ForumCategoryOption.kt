package com.thekeeperofpie.artistalleydatabase.anime.forums

import artistalleydatabase.modules.anime.forums.generated.resources.Res
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_anime
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_announcements
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_forum_games
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_gaming
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_general
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_light_novels
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_manga
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_music
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_news
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_recommendations
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_release_discussions
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_site_aniList_apps
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_site_bug_reports
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_site_feedback
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_site_miscellaneous
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_category_visual_novels
import org.jetbrains.compose.resources.StringResource

// TODO: Is there a way to get these programmatically?
enum class ForumCategoryOption(val textRes: StringResource, val categoryId: Int) {

    GENERAL(Res.string.anime_forum_category_general, 7),
    ANIME(Res.string.anime_forum_category_anime, 1),
    MANGA(Res.string.anime_forum_category_manga, 2),
    RELEASE_DISCUSSIONS(Res.string.anime_forum_category_release_discussions, 5),
    ANNOUNCEMENTS(Res.string.anime_forum_category_announcements, 13),
    NEWS(Res.string.anime_forum_category_news, 8),
    MUSIC(Res.string.anime_forum_category_music, 9),
    GAMING(Res.string.anime_forum_category_gaming, 10),
    VISUAL_NOVELS(Res.string.anime_forum_category_visual_novels, 4),
    LIGHT_NOVELS(Res.string.anime_forum_category_light_novels, 3),
    FORUM_GAMES(Res.string.anime_forum_category_forum_games, 16),
    RECOMMENDATIONS(Res.string.anime_forum_category_recommendations, 15),
    SITE_FEEDBACK(Res.string.anime_forum_category_site_feedback, 11),
    SITE_BUG_REPORTS(Res.string.anime_forum_category_site_bug_reports, 12),
    ANILIST_APPS(Res.string.anime_forum_category_site_aniList_apps, 18),
    MISCELLANEOUS(Res.string.anime_forum_category_site_miscellaneous, 17),
}
