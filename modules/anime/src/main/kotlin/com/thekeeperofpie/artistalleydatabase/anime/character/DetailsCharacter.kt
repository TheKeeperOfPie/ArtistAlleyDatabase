package com.thekeeperofpie.artistalleydatabase.anime.character

import com.anilist.fragment.CharacterNameLanguageFragment
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.StaffNavigationData

data class DetailsCharacter(
    val id: String,
    val name: CharacterNameLanguageFragment?,
    val image: String?,
    val languageToVoiceActor: Map<String, VoiceActor> = emptyMap(),
    val character: CharacterNavigationData?,
    val roleTextRes: Int? = null,
) {
    data class VoiceActor(
        val id: String,
        val name: String?,
        val image: String?,
        val language: String,
        val staff: StaffNavigationData,
    )
}
