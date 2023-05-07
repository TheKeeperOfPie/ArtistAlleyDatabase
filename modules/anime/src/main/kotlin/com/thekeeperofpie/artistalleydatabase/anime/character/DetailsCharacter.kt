package com.thekeeperofpie.artistalleydatabase.anime.character

data class DetailsCharacter(
    val id: String,
    val name: String?,
    val image: String?,
    val languageToVoiceActor: Map<String, VoiceActor>,
) {
    data class VoiceActor(
        val id: String,
        val name: String?,
        val image: String?,
        val language: String,
    )
}
