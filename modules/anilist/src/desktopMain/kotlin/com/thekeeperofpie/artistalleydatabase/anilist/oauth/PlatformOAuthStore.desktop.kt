package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.coroutines.flow.MutableStateFlow
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
actual class PlatformOAuthStore {
    // TODO
    internal actual val authTokenState = MutableStateFlow<String?>(null)

    internal actual suspend fun storeAuthTokenResult(token: String) {
    }

    internal actual suspend fun clearAuthToken() {
    }
}
