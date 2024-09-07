package com.thekeeperofpie.artistalleydatabase.vgmdb

import co.touchlab.kermit.Logger
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.AlbumColumnEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.album.DiscEntry
import com.thekeeperofpie.artistalleydatabase.vgmdb.artist.ArtistColumnEntry
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
class VgmdbJson(val json: Json) {

    companion object {
        private const val TAG = "VgmdbJson"
    }

    fun parseCatalogIdColumn(value: String?) = parseAlbumColumn(value)

    fun parseTitleColumn(value: String?) = parseAlbumColumn(value)

    fun parseDiscColumn(value: String): DiscEntry? {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            Logger.e(TAG) { "Error parsing disc column: $value" }
            null
        }
    }

    private fun parseAlbumColumn(value: String?): Either<String, AlbumColumnEntry> {
        if (value?.contains("{") == true) {
            try {
                return Either.Right(json.decodeFromString(value))
            } catch (e: Exception) {
                Logger.e(TAG) { "Error parsing album column: $value" }
            }
        }

        return Either.Left(value ?: "")
    }

    fun parseArtistColumn(value: String?): Either<String, ArtistColumnEntry> {
        if (value?.contains("{") == true) {
            try {
                return Either.Right(json.decodeFromString(value))
            } catch (e: Exception) {
                Logger.e(TAG) {"Error parsing artist column: $value" }
            }
        }

        return Either.Left(value ?: "")
    }
}
