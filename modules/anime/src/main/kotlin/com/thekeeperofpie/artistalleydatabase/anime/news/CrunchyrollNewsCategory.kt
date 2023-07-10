package com.thekeeperofpie.artistalleydatabase.anime.news

import androidx.annotation.StringRes
import com.thekeeperofpie.artistalleydatabase.anime.R

enum class CrunchyrollNewsCategory(val id: String, @StringRes val textRes: Int) {
    NEWS("News", R.string.anime_news_crunchyroll_category_news),
    FEATURES("Features", R.string.anime_news_crunchyroll_category_features),
    GUIDES("Guides", R.string.anime_news_crunchyroll_category_guides),
    ANNOUNCEMENTS("Announcements", R.string.anime_news_crunchyroll_category_announcements),
    QUIZZES("Quizzes", R.string.anime_news_crunchyroll_category_quizzes),
}
