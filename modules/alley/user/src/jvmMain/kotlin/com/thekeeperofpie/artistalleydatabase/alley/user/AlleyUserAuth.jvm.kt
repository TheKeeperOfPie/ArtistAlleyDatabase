package com.thekeeperofpie.artistalleydatabase.alley.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.UriHandler
import com.thekeeperofpie.artistalleydatabase.alley.models.network.BackendUserRequest
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.UserDataSyncPayload
import dev.zacsweers.metro.Inject
import io.ktor.http.HttpStatusCode

@Inject
actual class AlleyUserAuth {

    actual var isLoggedIn: Boolean by mutableStateOf(false)
        private set

    actual fun startAuth(uriHandler: UriHandler, url: String) {
        uriHandler.openUri(url)
    }

    actual suspend fun sync(payload: UserDataSyncPayload): Pair<HttpStatusCode, BackendUserRequest.Sync.Response>? = null
}
