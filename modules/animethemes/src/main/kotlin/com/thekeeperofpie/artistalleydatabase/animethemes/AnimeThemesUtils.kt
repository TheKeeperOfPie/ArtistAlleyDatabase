package com.thekeeperofpie.artistalleydatabase.animethemes

import com.thekeeperofpie.artistalleydatabase.animethemes.models.Anime
import com.thekeeperofpie.artistalleydatabase.animethemes.models.AnimeTheme

object AnimeThemesUtils {

    fun buildWebsiteLink(anime: Anime, animeTheme: AnimeTheme): String {
        val base = "https://animethemes.moe/anime/${anime.slug}/${animeTheme.slug}"
        val tag = animeTheme.animeThemeEntries.firstOrNull()?.tag
        return base + tag?.let { "-$it" }.orEmpty()
    }
}
