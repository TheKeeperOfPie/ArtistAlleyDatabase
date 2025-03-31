package com.thekeeperofpie.artistalleydatabase.anilist.data

import com.anilist.data.type.MediaType

object AniListDataUtils {
    const val ANILIST_BASE_URL = "https://anilist.co"

    fun mediaUrl(type: MediaType, id: String) = when (type) {
        MediaType.ANIME -> animeUrl(id)
        MediaType.MANGA -> mangaUrl(id)
        MediaType.UNKNOWN__ -> animeUrl(id)
    }

    fun animeUrl(id: String) = "$ANILIST_BASE_URL/anime/$id"
    fun mangaUrl(id: String) = "$ANILIST_BASE_URL/manga/$id"
}
