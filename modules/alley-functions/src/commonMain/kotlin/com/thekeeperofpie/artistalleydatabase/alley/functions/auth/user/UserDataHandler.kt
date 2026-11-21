package com.thekeeperofpie.artistalleydatabase.alley.functions.auth.user

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import com.thekeeperofpie.artistalleydatabase.alley.functions.user.AlleyUserDatabase
import com.thekeeperofpie.artistalleydatabase.alley.user.UserArtist
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload
import kotlin.time.Clock

object UserDataHandler {
    suspend fun syncFromClient(
        clock: Clock,
        userId: UserId,
        payload: UserDataSyncPayload,
        database: AlleyUserDatabase,
    ): UserDataSyncPayload {
        val newGeneration = database.transactionWithResult {
            val nextGeneration =
                database.generationQueries.incrementUserGeneration(userId).awaitAsOne()
            // TODO: Compare and handle mismatched last sync generation

            // TODO: Handle deletions
            payload.artists?.forEach {
                database.userArtistQueries.insertUserArtist(
                    UserArtist(
                        userId = userId,
                        dataYear = it.dataYear,
                        artistId = it.id,
                        favorite = it.favorite,
                        ignored = it.ignored,
                        generation = nextGeneration,
                        // Reduces accuracy, but ignores broken client time
                        updatedAt = clock.now(),
                    )
                )
            }
            nextGeneration
        }

        val artistChanges = database.userArtistQueries.getChangesBetween(
            userId = userId,
            lowerGeneration = payload.lastSyncGeneration ?: 0,
            upperGeneration = newGeneration,
        ).awaitAsList()

        // TODO: Remove the client's own uploaded changes so only the diff is passed back
        return UserDataSyncPayload(
            lastSyncGeneration = newGeneration,
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
        )
    }
}
