package com.thekeeperofpie.artistalleydatabase.alley.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.UriHandler
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyCryptography
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.web.cookieStore
import dev.zacsweers.metro.Inject
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Inject
actual class AlleyUserAuth(
    private val appScope: ApplicationScope,
    private val settings: AlleyUserSettings,
    private val userApi: AlleyUserApi,
) {

    actual var isLoggedIn: Boolean by mutableStateOf(false)
        private set

    // Block other requests until the auth code is checked,
    // otherwise a parallel request could clear the token
    private var checkedAuthCode = CompletableDeferred<Unit>()

    init {
        appScope.launch {
            try {
                isLoggedIn = cookieStore.get(COOKIE_CLIENT_AUTH_SIGNAL).await()?.value != null
                cookieStore.addEventListener("change") {
                    val changedAuth = it.changed.toList()
                        .find { it.name == COOKIE_CLIENT_AUTH_SIGNAL }
                    if (changedAuth != null) {
                        isLoggedIn = true
                    }
                    val deletedAuth = it.deleted.toList()
                        .find { it.name == COOKIE_CLIENT_AUTH_SIGNAL }
                    if (deletedAuth != null) {
                        isLoggedIn = false
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        appScope.launch {
            val pendingUserAuthCode = settings.pendingUserAuthCode
            if (!pendingUserAuthCode.isNullOrEmpty()) {
                userApi.exchangeAuthCode(pendingUserAuthCode)
                settings.pendingUserAuthCode = null
                delay(1.seconds)
            }
            checkedAuthCode.complete(Unit)
        }
    }

    actual fun startAuth(uriHandler: UriHandler, url: String) {
        appScope.launch {
            val keys = AlleyCryptography.generateOneTimeEncryptionKeys()
            // Writes backing the state are async, so navigating can stop the window before it
            // gets written, instead access the API directly to write the raw value
            localStorage.setItem("pendingUserAuthCode", keys.publicKey)
            window.location.href = url + "&state=${Uri.encode(keys.publicKey)}"
        }
    }

    actual fun checkAuth() {
        appScope.launch {
            checkedAuthCode.await()
            userApi.checkAuth()
        }
    }
}
