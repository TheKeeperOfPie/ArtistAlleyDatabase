package com.thekeeperofpie.artistalleydatabase.alley.models

enum class AniListType {
    NONE, ANIME, MANGA;

    companion object {
        fun parse(value: String?) = when {
            value.equals("ANIME", ignoreCase = true) -> ANIME
            value.equals("MANGA", ignoreCase = true) -> MANGA
            else -> NONE
        }
    }
}
