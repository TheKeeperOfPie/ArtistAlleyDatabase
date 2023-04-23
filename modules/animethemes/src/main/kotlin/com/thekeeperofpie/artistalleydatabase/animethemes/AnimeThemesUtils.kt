package com.thekeeperofpie.artistalleydatabase.animethemes

import com.thekeeperofpie.artistalleydatabase.animethemes.models.Anime
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeTheme

object AnimeThemesUtils {

    private const val BASE_URL = "https://animethemes.moe"

    fun buildWebsiteLink(anime: Anime, animeTheme: AnimeTheme): String {
        val base = "$BASE_URL/anime/${anime.slug}/${animeTheme.slug}"
        val tag = animeTheme.animeThemeEntries.firstOrNull()?.tag
        return base + tag?.let { "-$it" }.orEmpty()
    }

    fun artistUrl(slug: String) = "https://animethemes.moe/artist/$slug"
}
