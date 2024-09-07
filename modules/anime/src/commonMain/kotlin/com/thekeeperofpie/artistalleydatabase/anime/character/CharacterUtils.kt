package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.runtime.Composable
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_character_role_background
import artistalleydatabase.modules.anime.generated.resources.anime_character_role_main
import artistalleydatabase.modules.anime.generated.resources.anime_character_role_supporting
import artistalleydatabase.modules.anime.generated.resources.anime_character_role_unknown
import com.anilist.fragment.CharacterNameLanguageFragment
import com.anilist.fragment.DetailsCharacterEdge
import com.anilist.type.CharacterRole
import com.thekeeperofpie.artistalleydatabase.anilist.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.LocalLanguageOptionCharacters

object CharacterUtils {

    fun <CharacterEdge : DetailsCharacterEdge> toDetailsCharacters(
        edges: List<CharacterEdge?>?,
        role: ((CharacterEdge) -> CharacterRole?)? = null,
    ) = edges?.filterNotNull()?.map { toDetailsCharacter(it, role) }.orEmpty().distinctBy { it.id }

    fun <CharacterEdge : DetailsCharacterEdge> toDetailsCharacter(
        edge: CharacterEdge,
        role: ((CharacterEdge) -> CharacterRole?)? = null,
    ) = DetailsCharacter(
        id = edge.node.id.toString(),
        name = edge.node.name,
        image = edge.node.image?.large,
        languageToVoiceActor = edge.voiceActors?.filterNotNull()
            ?.mapNotNull {
                it.languageV2?.let { language ->
                    language to DetailsCharacter.VoiceActor(
                        id = it.id.toString(),
                        name = it.name?.userPreferred?.replace(Regex("\\s"), " "),
                        image = it.image?.large,
                        language = language,
                        staff = it,
                    )
                }
            }
            ?.associate { it }
            .orEmpty(),
        character = edge.node,
        roleTextRes = role?.invoke(edge)?.toTextRes(),
    )

    fun CharacterRole.toTextRes() = when (this) {
        CharacterRole.MAIN -> Res.string.anime_character_role_main
        CharacterRole.SUPPORTING -> Res.string.anime_character_role_supporting
        CharacterRole.BACKGROUND -> Res.string.anime_character_role_background
        CharacterRole.UNKNOWN__ -> Res.string.anime_character_role_unknown
    }

    @Composable
    fun CharacterNameLanguageFragment.primaryName() = primaryName(LocalLanguageOptionCharacters.current)

    fun CharacterNameLanguageFragment.primaryName(languageOption: AniListLanguageOption) =
        when (languageOption) {
            AniListLanguageOption.DEFAULT -> userPreferred
            AniListLanguageOption.NATIVE -> native
            AniListLanguageOption.ENGLISH,
            AniListLanguageOption.ROMAJI,
            -> full
        }

    @Composable
    fun CharacterNameLanguageFragment.subtitleName() = subtitleName(LocalLanguageOptionCharacters.current)

    fun CharacterNameLanguageFragment.subtitleName(languageOption: AniListLanguageOption) =
        when (languageOption) {
            AniListLanguageOption.DEFAULT -> native.takeIf { it != userPreferred } ?: full
            AniListLanguageOption.NATIVE -> full.takeIf { it != native } ?: userPreferred
            AniListLanguageOption.ENGLISH,
            AniListLanguageOption.ROMAJI,
            -> native.takeIf { it != full } ?: userPreferred
        }
}