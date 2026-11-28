package com.thekeeperofpie.artistalleydatabase.alley.user

import androidx.compose.ui.platform.UriHandler
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendUserRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload
import io.ktor.http.HttpStatusCode

expect class AlleyUserAuth {

    var isLoggedIn: Boolean
        private set

    fun startAuth(uriHandler: UriHandler, url: String)

    suspend fun sync(payload: UserDataSyncPayload): Pair<HttpStatusCode, BackendUserRequest.Sync.Response>?
}

@Suppress("UnusedReceiverParameter")
internal val AlleyUserAuth.COOKIE_CLIENT_AUTH_SIGNAL get() = "AADB_Authorized"
