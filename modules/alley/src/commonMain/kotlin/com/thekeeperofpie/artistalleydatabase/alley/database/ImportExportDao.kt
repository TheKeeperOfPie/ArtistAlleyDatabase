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

    suspend fun getExportPartialArtists2023() =
        database().userImportExportQueries.getExportPartialArtists2023().awaitAsList()

    suspend fun getExportPartialArtists2024() =
        database().userImportExportQueries.getExportPartialArtists2024().awaitAsList()

    suspend fun getExportPartialArtists2025() =
        database().userImportExportQueries.getExportPartialArtists2025().awaitAsList()

    suspend fun getExportPartialArtistsAnimeExpo2026() =
        database().userImportExportQueries.getExportPartialArtistsAnimeExpo2026().awaitAsList()

    suspend fun getExportPartialArtistsAnimeNyc2024() =
        database().userImportExportQueries.getExportPartialArtistsAnimeNyc2024().awaitAsList()

    suspend fun getExportPartialArtistsAnimeNyc2025() =
        database().userImportExportQueries.getExportPartialArtistsAnimeNyc2025().awaitAsList()

    suspend fun getExportPartialStampRallies2023() =
        database().userImportExportQueries.getExportPartialStampRallies2023().awaitAsList()

    suspend fun getExportPartialStampRallies2024() =
        database().userImportExportQueries.getExportPartialStampRallies2024().awaitAsList()

    suspend fun getExportPartialStampRallies2025() =
        database().userImportExportQueries.getExportPartialStampRallies2025().awaitAsList()

    suspend fun getExportPartialStampRalliesAnimeExpo2026() =
        database().userImportExportQueries.getExportPartialStampRalliesAnimeExpo2026().awaitAsList()

    suspend fun getExportFullArtists2023() =
        database().userImportExportQueries.getExportFullArtists2023().awaitAsList()

    suspend fun getExportFullArtists2024() =
        database().userImportExportQueries.getExportFullArtists2024().awaitAsList()

    suspend fun getExportFullArtists2025() =
        database().userImportExportQueries.getExportFullArtists2025().awaitAsList()

    suspend fun getExportFullArtistsAnimeExpo2026() =
        database().userImportExportQueries.getExportFullArtistsAnimeExpo2026().awaitAsList()

    suspend fun getExportFullArtistsAnimeNyc2024() =
        database().userImportExportQueries.getExportFullArtistsAnimeNyc2024().awaitAsList()

    suspend fun getExportFullArtistsAnimeNyc2025() =
        database().userImportExportQueries.getExportFullArtistsAnimeNyc2025().awaitAsList()

    suspend fun getExportFullStampRallies2023() =
        database().userImportExportQueries.getExportFullStampRallies2023().awaitAsList()

    suspend fun getExportFullStampRallies2024() =
        database().userImportExportQueries.getExportFullStampRallies2024().awaitAsList()

    suspend fun getExportFullStampRallies2025() =
        database().userImportExportQueries.getExportFullStampRallies2025().awaitAsList()

    suspend fun getExportFullStampRalliesAnimeExpo2026() =
        database().userImportExportQueries.getExportFullStampRalliesAnimeExpo2026().awaitAsList()

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
