package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow

@SingleIn(AppScope::class)
@Inject
actual class PlatformOAuthStore {
    // TODO
    internal actual val authTokenState = MutableStateFlow<String?>(null)

    internal actual suspend fun storeAuthTokenResult(token: String) {
    }

    internal actual suspend fun clearAuthToken() {
    }
}
