package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavorites2023
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavorites2024
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavorites2025
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavoritesAnimeExpo2026
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavoritesAnimeNyc2024
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavoritesAnimeNyc2025
import com.thekeeperofpie.artistalleydatabase.alley.UserEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.artist.BoothWithFavorite
import com.thekeeperofpie.artistalleydatabase.alley.settings.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.user.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.MerchUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.SeriesUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.user.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.utils.PersistentStorageRequester
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.milliseconds

private fun GetBoothsWithFavorites2023.toBoothWithFavorite() =
    BoothWithFavorite(
        year = DataYear.ANIME_EXPO_2023,
        id = id,
        booth = booth,
        name = name,
        images = images,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )

private fun GetBoothsWithFavorites2024.toBoothWithFavorite() =
    BoothWithFavorite(
        year = DataYear.ANIME_EXPO_2024,
        id = id,
        booth = booth,
        name = name,
        images = images,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )

private fun GetBoothsWithFavorites2025.toBoothWithFavorite() =
    BoothWithFavorite(
        year = DataYear.ANIME_EXPO_2025,
        id = id,
        booth = booth,
        name = name,
        images = images,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )

private fun GetBoothsWithFavoritesAnimeExpo2026.toBoothWithFavorite() =
    BoothWithFavorite(
        year = DataYear.ANIME_EXPO_2026,
        id = id,
        booth = booth,
        name = name,
        images = images,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )

private fun GetBoothsWithFavoritesAnimeNyc2024.toBoothWithFavorite() =
    BoothWithFavorite(
        year = DataYear.ANIME_NYC_2024,
        id = id,
        booth = booth,
        name = name,
        images = images,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )

private fun GetBoothsWithFavoritesAnimeNyc2025.toBoothWithFavorite() =
    BoothWithFavorite(
        year = DataYear.ANIME_NYC_2025,
        id = id,
        booth = booth,
        name = name,
        images = images,
        favorite = DaoUtils.coerceBooleanForJs(favorite),
    )

@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
class UserEntryDao(
    private val driver: suspend () -> SqlDriver,
    private val database: suspend () -> AlleySqlDatabase,
    private val settings: ArtistAlleySettings,
    private val dao: suspend () -> UserEntryQueries = { database().userEntryQueries },
) {
    companion object {
        private val NOTIFY_DELAY = 350.milliseconds
    }

    @Inject
    constructor(
        database: ArtistAlleyDatabase,
        settings: ArtistAlleySettings,
    ) : this(driver = database::driver, database = database::database, settings = settings)

    suspend fun getArtistFavorites() = dao()
        .getArtistFavorites()
        .asFlow()
        .mapToList(PlatformDispatchers.IO)

    fun getBoothsWithFavorites() = settings.dataYear
        .flatMapLatest { dataYear ->
            dao().run {
                when (dataYear) {
                    DataYear.ANIME_EXPO_2023 -> getBoothsWithFavorites2023()
                        .asFlow()
                        .mapToList(PlatformDispatchers.IO)
                        .map { dataYear to it.map { it.toBoothWithFavorite() } }
                    DataYear.ANIME_EXPO_2024 -> getBoothsWithFavorites2024()
                        .asFlow()
                        .mapToList(PlatformDispatchers.IO)
                        .map { dataYear to it.map { it.toBoothWithFavorite() } }
                    DataYear.ANIME_EXPO_2025 -> getBoothsWithFavorites2025()
                        .asFlow()
                        .mapToList(PlatformDispatchers.IO)
                        .map { dataYear to it.map { it.toBoothWithFavorite() } }
                    DataYear.ANIME_EXPO_2026 -> getBoothsWithFavoritesAnimeExpo2026()
                        .asFlow()
                        .mapToList(PlatformDispatchers.IO)
                        .map { dataYear to it.map { it.toBoothWithFavorite() } }
                    DataYear.ANIME_NYC_2024 -> getBoothsWithFavoritesAnimeNyc2024()
                        .asFlow()
                        .mapToList(PlatformDispatchers.IO)
                        .map { dataYear to it.map { it.toBoothWithFavorite() } }
                    DataYear.ANIME_NYC_2025 -> getBoothsWithFavoritesAnimeNyc2025()
                        .asFlow()
                        .mapToList(PlatformDispatchers.IO)
                        .map { dataYear to it.map { it.toBoothWithFavorite() } }
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
            delay(NOTIFY_DELAY)
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
            delay(NOTIFY_DELAY)
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
            delay(NOTIFY_DELAY)
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
            delay(NOTIFY_DELAY)
            driver().notifyListeners("merchEntry", "merchUserEntry")
        }
    }
}
