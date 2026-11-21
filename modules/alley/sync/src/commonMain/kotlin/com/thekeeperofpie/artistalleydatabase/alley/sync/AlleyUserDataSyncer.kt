package com.thekeeperofpie.artistalleydatabase.alley.sync

import com.thekeeperofpie.artistalleydatabase.alley.user.AlleyUserSettings
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload

class AlleyUserDataSyncer(
    private val userDataSyncDao: UserDataSyncDao,
    private val settings: AlleyUserSettings,
) {
    suspend fun getPayload(): UserDataSyncPayload {
        val lastSyncGeneration = settings.lastSyncGeneration ?: -1L
        val payload = userDataSyncDao.getPayload(lastSyncGeneration)
        return UserDataSyncPayload(
            lastSyncGeneration = lastSyncGeneration,
            artists = payload.artistChangesSince.map {
                UserDataSyncPayload.Artist(
                    generation = payload.lastGeneration ?: 0L,
                    dataYear = it.dataYear,
                    id = it.artistId,
                    favorite = it.favorite,
                    ignored = it.ignored,
                    updatedAt = it.updatedAt,
                )
            },
            rallies = payload.rallyChangesSince.map {
                UserDataSyncPayload.StampRally(
                    generation = payload.lastGeneration ?: 0L,
                    id = it.stampRallyId,
                    favorite = it.favorite,
                    ignored = it.ignored,
                    updatedAt = it.updatedAt,
                )
            },
            series = payload.seriesChangesSince.map {
                UserDataSyncPayload.Series(
                    generation = payload.lastGeneration ?: 0L,
                    id = it.seriesId,
                    favorite = it.favorite,
                    updatedAt = it.updatedAt,
                )
            },
            merch = payload.merchChangesSince.map {
                UserDataSyncPayload.Merch(
                    generation = payload.lastGeneration ?: 0L,
                    id = it.merchId,
                    favorite = it.favorite,
                    updatedAt = it.updatedAt,
                )
            },
        )
    }
}
