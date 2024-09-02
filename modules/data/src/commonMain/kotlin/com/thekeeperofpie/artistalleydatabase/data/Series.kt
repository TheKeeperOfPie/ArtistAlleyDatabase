package com.thekeeperofpie.artistalleydatabase.data

import co.touchlab.kermit.Logger
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaColumnEntry
import kotlinx.serialization.json.Json

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

        fun parseSingle(json: Json, seriesSerialized: String): Series {
            if (seriesSerialized.startsWith("{")) {
                try {
                    val entry = json.decodeFromString<MediaColumnEntry>(seriesSerialized)
                    return AniList(entry.id, seriesSerialized, entry)
                } catch (e: Exception) {
                    Logger.e(TAG, e) { "Fail to parse MediaColumnEntry: $seriesSerialized" }
                }
            }

            return Custom(seriesSerialized)
        }

        fun parse(json: Json, seriesSerialized: List<String>) =
            seriesSerialized.map { parseSingle(json, it) }
    }
}
