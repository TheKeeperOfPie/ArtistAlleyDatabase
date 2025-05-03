package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

class ImportExportDao(
    val database: suspend () -> AlleySqlDatabase,
) {
    suspend fun getExportPartialArtists2023() =
        database().userImportExportQueries.getExportPartialArtists2023().awaitAsList()

    suspend fun getExportPartialArtists2024() =
        database().userImportExportQueries.getExportPartialArtists2024().awaitAsList()

    suspend fun getExportPartialArtists2025() =
        database().userImportExportQueries.getExportPartialArtists2025().awaitAsList()

    suspend fun getExportPartialStampRallies2023() =
        database().userImportExportQueries.getExportPartialStampRallies2023().awaitAsList()

    suspend fun getExportPartialStampRallies2024() =
        database().userImportExportQueries.getExportPartialStampRallies2024().awaitAsList()

    suspend fun getExportPartialStampRallies2025() =
        database().userImportExportQueries.getExportPartialStampRallies2025().awaitAsList()

    suspend fun getExportFullArtists2023() =
        database().userImportExportQueries.getExportFullArtists2023().awaitAsList()

    suspend fun getExportFullArtists2024() =
        database().userImportExportQueries.getExportFullArtists2024().awaitAsList()

    suspend fun getExportFullArtists2025() =
        database().userImportExportQueries.getExportFullArtists2025().awaitAsList()

    suspend fun getExportFullStampRallies2023() =
        database().userImportExportQueries.getExportFullStampRallies2023().awaitAsList()

    suspend fun getExportFullStampRallies2024() =
        database().userImportExportQueries.getExportFullStampRallies2024().awaitAsList()

    suspend fun getExportFullStampRallies2025() =
        database().userImportExportQueries.getExportFullStampRallies2025().awaitAsList()

    suspend fun importArtist(
        artistId: String,
        dataYear: DataYear,
        favorite: Boolean,
        ignored: Boolean,
        notes: String? = null,
    ) = database().userImportExportQueries.run {
        importArtistUserEntry(artistId, dataYear, favorite, ignored)
        if (!notes.isNullOrBlank()) {
            importArtistNotes(artistId, dataYear, notes)
        }
    }

    suspend fun importStampRally(
        stampRallyId: String,
        favorite: Boolean,
        ignored: Boolean,
        notes: String? = null,
    ) = database().userImportExportQueries.run {
        importStampRallyUserEntry(stampRallyId, favorite, ignored)
        if (!notes.isNullOrBlank()) {
            importStampRallyNotes(stampRallyId, notes)
        }
    }

    suspend fun deleteUserData() = database().userImportExportQueries.deleteUserData()
}
