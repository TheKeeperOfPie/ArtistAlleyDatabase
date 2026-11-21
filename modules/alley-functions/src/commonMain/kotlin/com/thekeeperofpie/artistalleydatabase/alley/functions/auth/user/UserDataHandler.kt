package com.thekeeperofpie.artistalleydatabase.alley.functions.auth.user

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
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
    ): Long? = database.transactionWithResult {
        val previousUserGeneration =
            database.generationQueries.getUserGeneration(userId).awaitAsOneOrNull() ?: 0
        // TODO: Compare and handle mismatched last sync generation

        // TODO: Handle deletions
        val nextGeneration = previousUserGeneration + 1
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
}
