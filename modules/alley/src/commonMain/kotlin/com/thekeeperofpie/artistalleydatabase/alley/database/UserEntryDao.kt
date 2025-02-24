package com.thekeeperofpie.artistalleydatabase.alley.database

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.thekeeperofpie.artistalleydatabase.alley.AlleySqlDatabase
import com.thekeeperofpie.artistalleydatabase.alley.ArtistAlleySettings
import com.thekeeperofpie.artistalleydatabase.alley.ArtistUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavorites2023
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavorites2024
import com.thekeeperofpie.artistalleydatabase.alley.GetBoothsWithFavorites2025
import com.thekeeperofpie.artistalleydatabase.alley.StampRallyUserEntry
import com.thekeeperofpie.artistalleydatabase.alley.UserEntryQueries
import com.thekeeperofpie.artistalleydatabase.alley.artist.BoothWithFavorite
import com.thekeeperofpie.artistalleydatabase.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.PlatformDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

private fun GetBoothsWithFavorites2023.toBoothWithFavorite() =
    BoothWithFavorite(year = DataYear.YEAR_2023, id = id, booth = booth, favorite = favorite)

private fun GetBoothsWithFavorites2024.toBoothWithFavorite() =
    BoothWithFavorite(year = DataYear.YEAR_2024, id = id, booth = booth, favorite = favorite)

private fun GetBoothsWithFavorites2025.toBoothWithFavorite() =
    BoothWithFavorite(year = DataYear.YEAR_2025, id = id, booth = booth, favorite = favorite)

@OptIn(ExperimentalCoroutinesApi::class)
class UserEntryDao(
    private val database: suspend () -> AlleySqlDatabase,
    private val settings: ArtistAlleySettings,
    private val dao: suspend () -> UserEntryQueries = { database().userEntryQueries },
) {
    fun getBoothsWithFavorites() = settings.dataYear
        .flatMapLatest {
            when (it) {
                DataYear.YEAR_2023 -> dao().getBoothsWithFavorites2023()
                    .asFlow()
                    .mapToList(PlatformDispatchers.IO)
                    .map { it.map { it.toBoothWithFavorite() } }
                DataYear.YEAR_2024 -> dao().getBoothsWithFavorites2024()
                    .asFlow()
                    .mapToList(PlatformDispatchers.IO)
                    .map { it.map { it.toBoothWithFavorite() } }
                DataYear.YEAR_2025 -> dao().getBoothsWithFavorites2025()
                    .asFlow()
                    .mapToList(PlatformDispatchers.IO)
                    .map { it.map { it.toBoothWithFavorite() } }
            }
        }

    suspend fun insertArtistUserEntry(entry: ArtistUserEntry) {
        dao().transaction {
            val existing = dao().getArtistUserEntry(entry.artistId).awaitAsOneOrNull() ?: entry
            val newEntry = existing.copy(
                favorite = entry.favorite,
                ignored = entry.ignored,
                notes = entry.notes,
            )
            dao().insertArtistUserEntry(newEntry)
        }
    }

    suspend fun insertStampRallyUserEntry(entry: StampRallyUserEntry) {
        dao().transaction {
            val existing = dao().getStampRallyUserEntry(entry.stampRallyId).awaitAsOneOrNull()
                ?: entry
            val newEntry = existing.copy(
                favorite = entry.favorite,
                ignored = entry.ignored,
                notes = entry.notes,
            )
            dao().insertStampRallyUserEntry(newEntry)
        }
    }
}
