package com.thekeeperofpie.artistalleydatabase.anime.character

import androidx.compose.runtime.Immutable
import com.anilist.fragment.CharacterNameLanguageFragment
import com.anilist.fragment.CharacterNavigationData
import com.anilist.fragment.StaffNavigationData
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.emptyImmutableMap
import kotlinx.collections.immutable.ImmutableMap

@Immutable
data class DetailsCharacter(
    val id: String,
    val name: CharacterNameLanguageFragment?,
    val image: String?,
    val languageToVoiceActor: ImmutableMap<String, VoiceActor> = emptyImmutableMap(),
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
