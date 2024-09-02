package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import kotlinx.coroutines.flow.MutableStateFlow

expect class PlatformOAuthStore {
    internal val authTokenState: MutableStateFlow<String?>
    internal suspend fun storeAuthTokenResult(token: String)
    internal suspend fun clearAuthToken()
}
