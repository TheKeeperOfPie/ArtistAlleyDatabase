package com.thekeeperofpie.artistalleydatabase.anime.character

import com.anilist.fragment.DetailsCharacterEdge

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
                            )
                        }
                    }
                    ?.associate { it }
                    .orEmpty()
            )
        }.orEmpty().distinctBy { it.id }
}
