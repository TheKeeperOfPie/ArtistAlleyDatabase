package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.coroutines.flow.MutableStateFlow
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
actual class PlatformOAuthStore {
    internal actual val authTokenState: MutableStateFlow<String?>
        get() = TODO("Not yet implemented")

    internal actual fun storeAuthTokenResult(token: String) {
    }

    internal actual fun clearAuthToken() {
    }
}
