package com.thekeeperofpie.artistalleydatabase.anime.forum

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R

// TODO: Is there a way to get these programmatically?
enum class ForumCategoryOption(@StringRes val textRes: Int, val categoryId: Int) {

    GENERAL(R.string.anime_forum_category_general, 7),
    ANIME(R.string.anime_forum_category_anime, 1),
    MANGA(R.string.anime_forum_category_manga, 2),
    RELEASE_DISCUSSIONS(R.string.anime_forum_category_release_discussions, 5),
    ANNOUNCEMENTS(R.string.anime_forum_category_announcements, 13),
    NEWS(R.string.anime_forum_category_news, 8),
    MUSIC(R.string.anime_forum_category_music, 9),
    GAMING(R.string.anime_forum_category_gaming, 10),
    VISUAL_NOVELS(R.string.anime_forum_category_visual_novels, 4),
    LIGHT_NOVELS(R.string.anime_forum_category_light_novels, 3),
    FORUM_GAMES(R.string.anime_forum_category_forum_games, 16),
    RECOMMENDATIONS(R.string.anime_forum_category_recommendations, 15),
    SITE_FEEDBACK(R.string.anime_forum_category_site_feedback, 11),
    SITE_BUG_REPORTS(R.string.anime_forum_category_site_bug_reports, 12),
    ANILIST_APPS(R.string.anime_forum_category_site_aniList_apps, 18),
    MISCELLANEOUS(R.string.anime_forum_category_site_miscellaneous, 17),
}
