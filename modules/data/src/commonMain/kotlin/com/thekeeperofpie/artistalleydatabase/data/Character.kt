package com.thekeeperofpie.artistalleydatabase.data

import co.touchlab.kermit.Logger
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterColumnEntry
import kotlinx.serialization.json.Json

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

        fun parseSingle(json: Json, characterSerialized: String): Character {
            if (characterSerialized.startsWith("{")) {
                try {
                    val entry = json.decodeFromString<CharacterColumnEntry>(characterSerialized)
                    return AniList(entry.id, characterSerialized, entry)
                } catch (e: Exception) {
                    Logger.e(TAG, e) { "Fail to parse CharacterColumnEntry: $characterSerialized" }
                }
            }

            return Custom(characterSerialized)
        }

        fun parse(json: Json, charactersSerialized: List<String>) =
            charactersSerialized.map { parseSingle(json, it) }
    }
}
