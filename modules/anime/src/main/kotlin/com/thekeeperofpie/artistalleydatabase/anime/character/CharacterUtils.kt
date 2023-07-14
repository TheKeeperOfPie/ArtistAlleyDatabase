package com.thekeeperofpie.artistalleydatabase.anime.character

import com.anilist.fragment.DetailsCharacterEdge
import com.anilist.type.CharacterRole
import com.thekeeperofpie.artistalleydatabase.anime.R

object CharacterUtils {

    fun <CharacterEdge : DetailsCharacterEdge> toDetailsCharacters(
        edges: List<CharacterEdge?>?,
        role: ((CharacterEdge) -> CharacterRole?)? = null,
    ) = edges?.filterNotNull()?.map { toDetailsCharacter(it, role) }.orEmpty().distinctBy { it.id }

    fun <CharacterEdge : DetailsCharacterEdge> toDetailsCharacter(
        edge: CharacterEdge,
        role: ((CharacterEdge) -> CharacterRole?)? = null,
    ) = DetailsCharacter(
        id = edge.node?.id.toString(),
        name = edge.node?.name?.userPreferred,
        image = edge.node?.image?.large,
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
        CharacterRole.MAIN -> R.string.anime_character_role_main
        CharacterRole.SUPPORTING -> R.string.anime_character_role_supporting
        CharacterRole.BACKGROUND -> R.string.anime_character_role_background
        CharacterRole.UNKNOWN__ -> R.string.anime_character_role_unknown
    }

    fun subtitleName(userPreferred: String?, native: String?, full: String?) =
        if (native != userPreferred) {
            native
        } else if (full != userPreferred) {
            full
        } else {
            null
        }
}
