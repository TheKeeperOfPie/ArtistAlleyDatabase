package com.thekeeperofpie.artistalleydatabase.vgmdb

import android.util.Log
import com.thekeeperofpie.artistalleydatabase.android_utils.AppJson
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class VgmdbJson @Inject constructor(override val json: Json) : AppJson() {

    companion object {
        private const val TAG = "VgmdbJson"
    }

    fun parseCatalogIdColumn(value: String?) = parseAlbumColumn(value)

    fun parseTitleColumn(value: String?) = parseAlbumColumn(value)

    fun parseVocalistColumn(value: String?) = parseArtistColumn(value)

    fun parseComposerColumn(value: String?) = parseArtistColumn(value)

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

    private fun parseArtistColumn(value: String?): Either<String, ArtistColumnEntry> {
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