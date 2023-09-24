package com.thekeeperofpie.artistalleydatabase.anilist

import android.util.Log
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaColumnEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AniListJson(override val json: Json) : AppJson() {

    companion object {
        private const val TAG = "ArtJson"
    }

    fun parseSeriesColumn(value: String?): Either<String, MediaColumnEntry> {
        if (value?.contains("{") == true) {
            try {
                return Either.Right(json.decodeFromString(value))
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing series column: $value")
            }
        }

        return Either.Left(value ?: "")
    }

    fun parseCharacterColumn(value: String?): Either<String, CharacterColumnEntry> {
        if (value?.contains("{") == true) {
            try {
                return Either.Right(json.decodeFromString(value))
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing character column: $value")
            }
        }

        return Either.Left(value ?: "")
    }

    fun toJson(entry: CharacterColumnEntry) = json.encodeToString(entry)

    fun toJson(entry: MediaColumnEntry) = json.encodeToString(entry)
}
