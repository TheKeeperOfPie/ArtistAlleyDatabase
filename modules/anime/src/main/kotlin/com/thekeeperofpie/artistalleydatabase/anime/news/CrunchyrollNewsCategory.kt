package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R

enum class CrunchyrollNewsCategory(val id: String, @StringRes val textRes: Int) {
    NEWS("News", R.string.anime_news_crunchyroll_category_news),
    ANNOUNCEMENTS("Announcements", R.string.anime_news_crunchyroll_category_announcements),
    FEATURES("Features", R.string.anime_news_crunchyroll_category_features),
    GUIDES("Guides", R.string.anime_news_crunchyroll_category_guides),
    INTERVIEWS("Interviews", R.string.anime_news_crunchyroll_category_interviews),
    QUIZZES("Quizzes", R.string.anime_news_crunchyroll_category_quizzes),
    UNKNOWN("Unknown", R.string.anime_news_network_category_unknown),
}
