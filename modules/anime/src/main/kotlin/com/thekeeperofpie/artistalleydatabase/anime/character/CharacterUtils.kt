package com.thekeeperofpie.artistalleydatabase.anime.character

import com.anilist.fragment.DetailsCharacterEdge
import com.anilist.type.CharacterRole
import com.thekeeperofpie.artistalleydatabase.anime.R

object CharacterUtils {

    fun toDetailsCharacters(edges: List<DetailsCharacterEdge?>?) =
        edges?.filterNotNull()?.map {
            DetailsCharacter(
                id = it.node?.id.toString(),
                name = it.node?.name?.userPreferred,
                image = it.node?.image?.large,
                languageToVoiceActor = it.voiceActors?.filterNotNull()
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
                character = it.node,
            )
        }.orEmpty().distinctBy { it.id }

    fun CharacterRole.toTextRes() = when (this) {
        CharacterRole.MAIN -> R.string.anime_character_role_main
        CharacterRole.SUPPORTING -> R.string.anime_character_role_supporting
        CharacterRole.BACKGROUND -> R.string.anime_character_role_background
        CharacterRole.UNKNOWN__ -> R.string.anime_character_role_unknown
    }
}
