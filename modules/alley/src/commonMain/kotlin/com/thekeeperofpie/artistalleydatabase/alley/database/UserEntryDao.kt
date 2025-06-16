package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavorites2023
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavorites2024
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavorites2025
import com.thekeeperofpie.artistalleydatabase.alley.UserEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.artist.BoothWithFavorite
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.utils.PersistentStorageRequester
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.seconds

private fun GetBoothsWithFavorites2023.toBoothWithFavorite() =
    BoothWithFavorite(
        year = DataYear.YEAR_2023,
        id = id,
        booth = booth,
        name = name,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )

private fun GetBoothsWithFavorites2024.toBoothWithFavorite() =
    BoothWithFavorite(
        year = DataYear.YEAR_2024,
        id = id,
        booth = booth,
        name = name,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )

private fun GetBoothsWithFavorites2025.toBoothWithFavorite() =
    BoothWithFavorite(
        year = DataYear.YEAR_2025,
        id = id,
        booth = booth,
        name = name,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )

@OptIn(ExperimentalCoroutinesApi::class)
class UserEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val settings: ArtistAlleySettings,
    private val dao: suspend () -> UserEntryQueries = { database().userEntryQueries },
) {
    suspend fun getArtistFavorites() = dao()
        .getArtistFavorites()
        .asFlow()
        .mapToList(PlatformDispatchers.IO)

    fun getBoothsWithFavorites() = settings.dataYear
        .flatMapLatest {
            dao().run {
                when (it) {
                    DataYear.YEAR_2023 -> getBoothsWithFavorites2023()
                        .asFlow()
                        .mapToList(PlatformDispatchers.IO)
                        .map { it.map { it.toBoothWithFavorite() } }
                    DataYear.YEAR_2024 -> getBoothsWithFavorites2024()
                        .asFlow()
                        .mapToList(PlatformDispatchers.IO)
                        .map { it.map { it.toBoothWithFavorite() } }
                    DataYear.YEAR_2025 -> getBoothsWithFavorites2025()
                        .asFlow()
                        .mapToList(PlatformDispatchers.IO)
                        .map { it.map { it.toBoothWithFavorite() } }
                }
            }
        }

    suspend fun insertArtistUserEntry(entry: ArtistUserEntry) {
        PersistentStorageRequester.requestPersistent()
        dao().run {
            transaction {
                val existing =
                    getArtistUserEntry(entry.artistId, entry.dataYear).awaitAsOneOrNull() ?: entry
                val newEntry = existing.copy(
                    favorite = entry.favorite,
                    ignored = entry.ignored,
                )
                insertArtistUserEntry(newEntry)
            }
            delay(1.seconds)
            driver().notifyListeners("artistEntry", "artistUserEntry")
        }
    }

    suspend fun insertStampRallyUserEntry(entry: StampRallyUserEntry) {
        PersistentStorageRequester.requestPersistent()
        dao().run {
            transaction {
                val existing = getStampRallyUserEntry(entry.stampRallyId).awaitAsOneOrNull()
                    ?: entry
                val newEntry = existing.copy(
                    favorite = entry.favorite,
                    ignored = entry.ignored,
                )
                insertStampRallyUserEntry(newEntry)
            }
            delay(1.seconds)
            driver().notifyListeners("stampRallyEntry", "stampRallyUserEntry")
        }
    }

    suspend fun insertSeriesUserEntry(entry: SeriesUserEntry) {
        PersistentStorageRequester.requestPersistent()
        dao().run {
            transaction {
                val existing = getSeriesUserEntry(entry.seriesId).awaitAsOneOrNull()
                    ?: entry
                val newEntry = existing.copy(favorite = entry.favorite)
                insertSeriesUserEntry(newEntry)
            }
            delay(1.seconds)
            driver().notifyListeners("seriesEntry", "seriesUserEntry")
        }
    }

    suspend fun insertMerchUserEntry(entry: MerchUserEntry) {
        PersistentStorageRequester.requestPersistent()
        dao().run {
            transaction {
                val existing = getMerchUserEntry(entry.merchId).awaitAsOneOrNull()
                    ?: entry
                val newEntry = existing.copy(favorite = entry.favorite)
                insertMerchUserEntry(newEntry)
            }
            delay(1.seconds)
            driver().notifyListeners("merchEntry", "merchUserEntry")
        }
    }
}
