package com.thekeeperofpie.artistalleydatabase.anilist

import co.touchlab.kermit.Logger
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaColumnEntry
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.Either
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class AniListJson(val json: Json) {

    companion object {
        private const val TAG = "ArtJson"
    }

    fun parseSeriesColumn(value: String?): Either<String, MediaColumnEntry> {
        if (value?.contains("{") == true) {
            try {
                return Either.Right(json.decodeFromString(value))
            } catch (e: Exception) {
                Logger.e(TAG, e) { "Error parsing series column: $value" }
            }
        }

        return Either.Left(value ?: "")
    }

    fun parseCharacterColumn(value: String?): Either<String, CharacterColumnEntry> {
        if (value?.contains("{") == true) {
            try {
                return Either.Right(json.decodeFromString(value))
            } catch (e: Exception) {
                Logger.e(TAG, e) { "Error parsing character column: $value" }
            }
        }

        return Either.Left(value ?: "")
    }

    fun toJson(entry: CharacterColumnEntry) = json.encodeToString(entry)

    fun toJson(entry: MediaColumnEntry) = json.encodeToString(entry)
}
