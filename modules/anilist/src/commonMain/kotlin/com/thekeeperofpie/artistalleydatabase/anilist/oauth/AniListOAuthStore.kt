package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkAuthProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class AniListOAuthStore(
    scope: ApplicationScope,
    private val platformOAuthStore: PlatformOAuthStore,
    aniListSettings: AniListSettings,
) : NetworkAuthProvider {

    private val authTokenState = platformOAuthStore.authTokenState
    val authToken = combine(
        authTokenState,
        aniListSettings.ignoreViewer
    ) { authToken, ignore -> authToken.takeUnless { ignore } }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    private val authTokenMutex = Mutex()

    override val host: String = AniListUtils.GRAPHQL_API_HOST

    override val authHeader get() = authToken.value?.let { "Bearer $it" }

    val hasAuth = authToken.map { !it.isNullOrBlank() }

    suspend fun storeAuthTokenResult(token: String) {
        platformOAuthStore.storeAuthTokenResult(token)

        authTokenMutex.withLock {
            authTokenState.emit(token)
        }
    }

    suspend fun clearAuthToken() {
        platformOAuthStore.clearAuthToken()

        authTokenMutex.withLock {
            authTokenState.emit(null)
        }
    }
}
