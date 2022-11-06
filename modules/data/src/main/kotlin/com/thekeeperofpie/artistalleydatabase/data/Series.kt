package com.thekeeperofpie.artistalleydatabase.data

import android.util.Log
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaColumnEntry
import kotlinx.serialization.decodeFromString

sealed interface Series {
    val id: String
    val text: String

    data class AniList(
        override val id: String,
        override val text: String,
        val entry: MediaColumnEntry
    ) : Series

    data class Custom(override val text: String, override val id: String = text) : Series

    companion object {

        private val TAG = Series::class.java.name

        fun parseSingle(appJson: AppJson, seriesSerialized: String): Series {
            if (seriesSerialized.startsWith("{")) {
                try {
                    val entry = appJson.json.decodeFromString<MediaColumnEntry>(seriesSerialized)
                    return AniList(entry.id, seriesSerialized, entry)
                } catch (e: Exception) {
                    Log.e(TAG, "Fail to parse MediaColumnEntry: $seriesSerialized")
                }
            }

            return Custom(seriesSerialized)
        }

        fun parse(appJson: AppJson, seriesSerialized: List<String>) =
            seriesSerialized.map { parseSingle(appJson, it) }
    }
}