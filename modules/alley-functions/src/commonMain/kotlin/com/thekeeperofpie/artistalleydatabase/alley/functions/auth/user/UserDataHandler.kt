package com.thekeeperofpie.artistalleydatabase.alley.functions.auth.user

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import com.thekeeperofpie.artistalleydatabase.alley.functions.user.AlleyUserDatabase
import com.thekeeperofpie.artistalleydatabase.alley.user.UserArtist
import com.thekeeperofpie.artistalleydatabase.alley.user.UserSeries
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload
import kotlin.time.Clock

object UserDataHandler {
    suspend fun syncFromClient(
        clock: Clock,
        userId: UserId,
        payload: UserDataSyncPayload,
        database: AlleyUserDatabase,
    ): Pair<Short, UserDataSyncPayload> {
        val currentGeneration =
            database.generationQueries.getUserGeneration(userId).awaitAsOne()
        val httpStatus = when {
            // Server was reset, rebase everything
            currentGeneration < (payload.generation ?: 0) -> 409
            // Client isn't up to date with the server, just return a sync payload
            payload.generation != currentGeneration -> 409
            else -> 200
        }

        val lowerGeneration = when {
            // Server was reset, set lower to 0 and send everything
            currentGeneration < (payload.generation ?: 0) -> 0
            else -> payload.generation ?: 0L
        }

        val upperGeneration = when {
            currentGeneration < (payload.generation ?: 0) -> currentGeneration
            payload.generation != currentGeneration -> currentGeneration
            payload.isEmpty() -> currentGeneration
            else -> commitDiff(clock, database, userId, payload)
        }

        return httpStatus.toShort() to getChangesBetween(
            database = database,
            userId = userId,
            lowerGeneration = lowerGeneration,
            upperGeneration = upperGeneration,
        )
    }

    private suspend fun getChangesBetween(
        database: AlleyUserDatabase,
        userId: UserId,
        lowerGeneration: Long,
        upperGeneration: Long,
    ): UserDataSyncPayload {
        val artistChanges = database.userArtistQueries.getChangesBetween(
            userId = userId,
            lowerGeneration = lowerGeneration,
            upperGeneration = upperGeneration,
        ).awaitAsList()

        val seriesChanges = database.userSeriesQueries.getChangesBetween(
            userId = userId,
            lowerGeneration = lowerGeneration,
            upperGeneration = upperGeneration,
        ).awaitAsList()

        // TODO: Remove the client's own uploaded changes so only the diff is passed back
        return UserDataSyncPayload(
            generation = upperGeneration,
            artists = artistChanges.map {
                UserDataSyncPayload.Artist(
                    generation = it.generation,
                    dataYear = it.dataYear,
                    id = it.artistId,
                    favorite = it.favorite,
                    ignored = it.ignored,
                    updatedAt = it.updatedAt,
                )
            },
            series = seriesChanges.map {
                UserDataSyncPayload.Series(
                    generation = it.generation,
                    id = it.seriesId,
                    favorite = it.favorite,
                    updatedAt = it.updatedAt,
                )
            },
        )
    }

    private suspend fun commitDiff(
        clock: Clock,
        database: AlleyUserDatabase,
        userId: UserId,
        payload: UserDataSyncPayload,
    ): Long {
        // Must be queried outside transaction because Cloudflare D1 cannot return results in batch
        val nextGeneration =
            database.generationQueries.incrementUserGeneration(userId).awaitAsOne()
        return database.transactionWithResult {
            // TODO: Handle deletions
            payload.artists?.forEach {
                val now = clock.now()
                database.userArtistQueries.insertUserArtist(
                    UserArtist(
                        userId = userId,
                        dataYear = it.dataYear,
                        artistId = it.id,
                        favorite = it.favorite,
                        ignored = it.ignored,
                        generation = nextGeneration,
                        updatedAt = it.updatedAt?.coerceAtMost(now) ?: now,
                    )
                )
            }
            payload.series?.forEach {
                val now = clock.now()
                database.userSeriesQueries.insertUserSeries(
                    UserSeries(
                        userId = userId,
                        seriesId = it.id,
                        favorite = it.favorite,
                        generation = nextGeneration,
                        updatedAt = it.updatedAt?.coerceAtMost(now) ?: now,
                    )
                )
            }
            nextGeneration
        }
    }
}
