package com.thekeeperofpie.artistalleydatabase.anime

import androidx.annotation.StringRes

enum class AnimeNavDestinations(val id: String, @StringRes val textRes: Int) {

    LIST("anime_list", R.string.anime_screen_user_list),
    SEARCH("anime_search", R.string.anime_screen_search),

    ;

    companion object {
        // TODO: Make this configurable
        const val DEFAULT_INDEX = 1
    }
}