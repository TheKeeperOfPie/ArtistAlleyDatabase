package com.thekeeperofpie.artistalleydatabase.anime.character

import com.anilist.fragment.CharacterNavigationData

data class DetailsCharacter(
    val id: String,
    val name: String?,
    val image: String?,
    val languageToVoiceActor: Map<String, VoiceActor> = emptyMap(),
    val character: CharacterNavigationData?,
) {
    data class VoiceActor(
        val id: String,
        val name: String?,
        val image: String?,
        val language: String,
    )
}
