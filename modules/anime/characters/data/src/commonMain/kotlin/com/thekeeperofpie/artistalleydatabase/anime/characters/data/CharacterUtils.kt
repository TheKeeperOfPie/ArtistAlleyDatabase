package com.thekeeperofpie.artistalleydatabase.anime.characters.data

import androidx.compose.runtime.Composable
import artistalleydatabase.modules.anime.characters.data.generated.resources.Res
import artistalleydatabase.modules.anime.characters.data.generated.resources.anime_character_role_background
import artistalleydatabase.modules.anime.characters.data.generated.resources.anime_character_role_main
import artistalleydatabase.modules.anime.characters.data.generated.resources.anime_character_role_supporting
import artistalleydatabase.modules.anime.characters.data.generated.resources.anime_character_role_unknown
import com.anilist.data.fragment.CharacterNameLanguageFragment
import com.anilist.data.fragment.DetailsCharacterEdge
import com.anilist.data.type.CharacterRole
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListLanguageOption
import com.thekeeperofpie.artistalleydatabase.anilist.data.LocalLanguageOptionCharacters

object CharacterUtils {

    fun <CharacterEdge : DetailsCharacterEdge> toDetailsCharacters(
        edges: List<CharacterEdge?>?,
        role: ((CharacterEdge) -> CharacterRole?)? = null,
    ) = edges?.filterNotNull()?.map { toDetailsCharacter(it, role) }.orEmpty().distinctBy { it.id }

    fun <CharacterEdge : DetailsCharacterEdge> toDetailsCharacter(
        edge: CharacterEdge,
        role: ((CharacterEdge) -> CharacterRole?)? = null,
    ) = CharacterDetails(
        id = edge.node.id.toString(),
        name = edge.node.name,
        image = edge.node.image?.large,
        languageToVoiceActor = edge.voiceActors?.filterNotNull()
            ?.mapNotNull {
                it.languageV2?.let { language ->
                    language to CharacterDetails.VoiceActor(
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
