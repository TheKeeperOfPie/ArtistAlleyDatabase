package com.thekeeperofpie.artistalleydatabase.vgmdb

import android.util.Log
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.DiscEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import kotlinx.serialization.json.Json

class VgmdbJson(override val json: Json) : AppJson() {

    companion object {
        private const val TAG = "VgmdbJson"
    }

    fun parseCatalogIdColumn(value: String?) = parseAlbumColumn(value)

    fun parseTitleColumn(value: String?) = parseAlbumColumn(value)

    fun parseDiscColumn(value: String): DiscEntry? {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing disc column: $value")
            null
        }
    }

    private fun parseAlbumColumn(value: String?): Either<String, AlbumColumnEntry> {
        if (value?.contains("{") == true) {
            try {
                return Either.Right(json.decodeFromString(value))
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing album column: $value")
            }
        }

        return Either.Left(value ?: "")
    }

    fun parseArtistColumn(value: String?): Either<String, ArtistColumnEntry> {
        if (value?.contains("{") == true) {
            try {
                return Either.Right(json.decodeFromString(value))
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing artist column: $value")
            }
        }

        return Either.Left(value ?: "")
    }
}
