package com.thekeeperofpie.artistalleydatabase.art.json

import android.util.Log
import com.thekeeperofpie.artistalleydatabase.anilist.CharacterColumnEntry
import com.thekeeperofpie.artistalleydatabase.anilist.MediaColumnEntry
import com.thekeeperofpie.artistalleydatabase.utils.AppJson
import com.thekeeperofpie.artistalleydatabase.utils.Either
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ArtJson @Inject constructor(override val json: Json): AppJson() {

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