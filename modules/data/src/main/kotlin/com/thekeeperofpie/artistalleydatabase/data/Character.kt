package com.thekeeperofpie.artistalleydatabase.data

import android.util.Log
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterColumnEntry
import kotlinx.serialization.decodeFromString

sealed class Character {
    abstract val id: String
    abstract val text: String

    data class AniList(
        override val id: String,
        override val text: String,
        val entry: CharacterColumnEntry
    ) : Character()

    data class Custom(override val text: String, override val id: String = text) : Character()

    companion object {

        private val TAG = Character::class.java.name

        fun parseSingle(appJson: AppJson, characterSerialized: String): Character {
            if (characterSerialized.startsWith("{")) {
                try {
                    val entry =
                        appJson.json.decodeFromString<CharacterColumnEntry>(characterSerialized)
                    return AniList(entry.id, characterSerialized, entry)
                } catch (e: Exception) {
                    Log.e(TAG, "Fail to parse CharacterColumnEntry: $characterSerialized")
                }
            }

            return Custom(characterSerialized)
        }

        fun parse(appJson: AppJson, charactersSerialized: List<String>) =
            charactersSerialized.map { parseSingle(appJson, it) }
    }
}