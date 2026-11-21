package com.thekeeperofpie.artistalleydatabase.alley.models.network

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload
import kotlinx.serialization.Serializable

@Serializable
sealed interface BackendUserRequest {

    // Split out because KotlinX Serialization doesn't support <*> star type parameters
    interface WithResponse<Response>

    @Serializable
    data class Sync(
        val syncPayload: UserDataSyncPayload,
    ) : BackendUserRequest, WithResponse<Sync.Response> {

        @Serializable
        data class Response(
            val payload: UserDataSyncPayload,
        )
    }
}
