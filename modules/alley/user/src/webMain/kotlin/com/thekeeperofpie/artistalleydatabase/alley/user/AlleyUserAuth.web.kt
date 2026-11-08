package com.thekeeperofpie.artistalleydatabase.alley.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.ApplicationScope
import com.thekeeperofpie.artistalleydatabase.utils.web.cookieStore
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.await
import kotlinx.coroutines.launch

@Inject
actual class AlleyUserAuth(appScope: ApplicationScope) {

    actual var isLoggedIn: Boolean by mutableStateOf(false)
        private set

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
    }
}
