package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@SingleIn(AppScope::class)
class ImportExportDao(
    val driver: suspend () -> SqlDriver,
    val database: suspend () -> AlleySqlDatabase,
) {
    @Inject
    constructor(database: ArtistAlleyDatabase) : this(
        driver = database::driver,
        database = database::database,
    )

    suspend fun getExportPartialArtists(dataYear: DataYear) =
        database().userImportExportQueries.getExportPartialArtists(dataYear).awaitAsList()

    suspend fun getExportFullArtists(dataYear: DataYear) =
        database().userImportExportQueries.getExportFullArtists(dataYear).awaitAsList()

    suspend fun getExportPartialStampRallies(dataYear: DataYear) =
        database().userImportExportQueries.getExportPartialStampRallies(dataYear).awaitAsList()

    suspend fun getExportFullStampRallies(dataYear: DataYear) =
        database().userImportExportQueries.getExportFullStampRallies(dataYear).awaitAsList()

    suspend fun getExportFullSeries() =
        database().userImportExportQueries.getExportFullSeries().awaitAsList()

    suspend fun getExportFullMerch() =
        database().userImportExportQueries.getExportFullMerch().awaitAsList()

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

    suspend fun importSeries(
        seriesId: String,
        favorite: Boolean,
    ) = database().userImportExportQueries.run {
        importSeriesUserEntry(seriesId, favorite)
    }

    suspend fun importMerch(
        merchId: String,
        favorite: Boolean,
    ) = database().userImportExportQueries.run {
        importMerchUserEntry(merchId, favorite)
    }

    suspend fun notifyChange() {
        driver().notifyListeners(
            "artistNotes",
            "artistUserEntry",
            "stampRallyNotes",
            "stampRallyUserEntry",
            "seriesUserEntry",
            "merchUserEntry",
        )
    }

    suspend fun deleteUserData() {
        database().userImportExportQueries.run {
            transaction {
                deleteArtistNotes()
                deleteArtistUserEntry()
                deleteStampRallyNotes()
                deleteStampRallyUserEntry()
                deleteSeriesUserEntry()
                deleteMerchUserEntry()
            }
        }

        delay(1.seconds)
        notifyChange()
    }
}
