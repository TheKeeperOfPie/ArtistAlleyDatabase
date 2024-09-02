package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import kotlinx.coroutines.flow.MutableStateFlow

expect class PlatformOAuthStore {
    internal val authTokenState: MutableStateFlow<String?>
    internal fun storeAuthTokenResult(token: String)
    internal fun clearAuthToken()
}
