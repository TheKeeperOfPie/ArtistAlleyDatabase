package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear

class ImportExportDao(
    val database: suspend () -> AlleySqlDatabase,
) {
    suspend fun getExportDataArtists2023() =
        database().userImportExportQueries.getExportDataArtists2023().awaitAsList()

    suspend fun getExportDataArtists2024() =
        database().userImportExportQueries.getExportDataArtists2024().awaitAsList()

    suspend fun getExportDataArtists2025() =
        database().userImportExportQueries.getExportDataArtists2025().awaitAsList()

    suspend fun getExportDataStampRallies2023() =
        database().userImportExportQueries.getExportDataStampRallies2023().awaitAsList()

    suspend fun getExportDataStampRallies2024() =
        database().userImportExportQueries.getExportDataStampRallies2024().awaitAsList()

    suspend fun getExportDataStampRallies2025() =
        database().userImportExportQueries.getExportDataStampRallies2025().awaitAsList()

    suspend fun importArtist(artistId: String, dataYear: DataYear, favorite: Boolean, ignored: Boolean) {
        database().userImportExportQueries.importArtistUserEntry(artistId, dataYear, favorite, ignored)
    }

    suspend fun importStampRally(stampRallyId: String, favorite: Boolean, ignored: Boolean) {
        database().userImportExportQueries.importStampRallyUserEntry(stampRallyId, favorite, ignored)
    }
}
