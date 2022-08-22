package com.thekeeperofpie.artistalleydatabase.anilist

import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntry

object AniListUtils {

    private const val ANILIST_BASE_URL = "https://anilist.co"

    fun characterUrl(id: Int) = "$ANILIST_BASE_URL/character/$id"

    fun mediaUrl(type: MediaType?, id: Int) = when(type){
        MediaType.ANIME -> animeUrl(id)
        MediaType.MANGA -> mangaUrl(id)
        else -> null
    }

    fun mediaUrl(type: MediaEntry.Type?, id: Int) = when(type){
        MediaEntry.Type.ANIME -> animeUrl(id)
        MediaEntry.Type.MANGA -> mangaUrl(id)
        else -> null
    }
    fun animeUrl(id: Int) = "$ANILIST_BASE_URL/anime/$id"
    fun mangaUrl(id: Int) = "$ANILIST_BASE_URL/manga/$id"
}