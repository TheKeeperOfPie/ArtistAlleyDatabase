package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.runtime.Immutable
import com.anilist.data.fragment.CharacterNameLanguageFragment
import com.anilist.data.fragment.CharacterNavigationData
import com.anilist.data.fragment.StaffNavigationData
import org.jetbrains.compose.resources.StringResource

@Immutable
data class DetailsCharacter(
    val id: String,
    val name: CharacterNameLanguageFragment?,
    val image: String?,
    val languageToVoiceActor: Map<String, VoiceActor> = emptyMap(),
    val character: CharacterNavigationData?,
    val roleTextRes: StringResource? = null,
) {
    data class VoiceActor(
        val id: String,
        val name: String?,
        val image: String?,
        val language: String,
        val staff: StaffNavigationData,
    ) {
        val idWithRole = "$id-$language"
    }
}
