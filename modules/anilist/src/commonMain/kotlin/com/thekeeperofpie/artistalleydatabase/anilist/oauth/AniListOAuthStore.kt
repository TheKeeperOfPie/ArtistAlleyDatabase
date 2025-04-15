package com.thekeeperofpie.artistalleydatabase.anilist.oauth

import com.thekeeperofpie.artistalleydatabase.anilist.AniListSettings
import com.thekeeperofpie.artistalleydatabase.anilist.data.AniListDataUtils
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.combineStates
import com.thekeeperofpie.artistalleydatabase.utils_network.NetworkAuthProvider
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class AniListOAuthStore(
    scope: ApplicationScope,
    private val platformOAuthStore: PlatformOAuthStore,
    aniListSettings: AniListSettings,
) : NetworkAuthProvider {

    private val authTokenState = platformOAuthStore.authTokenState
    val authToken = combineStates(
        authTokenState,
        aniListSettings.ignoreViewer
    ) { authToken, ignore -> authToken.takeUnless { ignore } }

    override val host: String = AniListDataUtils.GRAPHQL_API_HOST

    override val authHeader get() = authToken.value?.let { "Bearer $it" }

    val hasAuth = authToken.map { it != null }

    suspend fun storeAuthTokenResult(token: String) {
        platformOAuthStore.storeAuthTokenResult(token)
    }

    suspend fun clearAuthToken() {
        platformOAuthStore.clearAuthToken()
    }
}
