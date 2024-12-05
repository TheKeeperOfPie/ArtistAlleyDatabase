package com.thekeeperofpie.artistalleydatabase.anime.songs

import androidx.compose.runtime.Composable
import com.anilist.data.fragment.CharacterNameLanguageFragment
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterUtils.primaryName

data class AnimeSongEntry(
    val id: String,
    val type: Type?,
    val title: String,
    val spoiler: Boolean,
    val artists: List<Artist>,
    val episodes: String?,
    val videoUrl: String?,
    val audioUrl: String?,
    val link: String?,
) {
    enum class Type {
        OP, ED
    }

    data class Artist(
        val id: String,
        val aniListId: String?,
        val animeThemesSlug: String,
        val name: String,
        val image: String?,
        val asCharacter: Boolean,
        val character: Character?,
        val link: String,
    ) {
        data class Character(
            val aniListId: String,
            val image: String?,
            private val name: CharacterNameLanguageFragment?,
            private val fallbackName: String?,
        ) {
            val link by lazy { AniListUtils.characterUrl(aniListId) }

            @Composable
            fun name() = name?.primaryName() ?: fallbackName ?: ""
        }
    }
}
