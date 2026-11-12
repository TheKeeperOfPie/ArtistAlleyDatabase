package com.thekeeperofpie.artistalleydatabase.alley.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.UriHandler
import dev.zacsweers.metro.Inject

@Inject
actual class AlleyUserAuth {

    actual var isLoggedIn: Boolean by mutableStateOf(false)
        private set

    actual fun startAuth(uriHandler: UriHandler, url: String) {
        uriHandler.openUri(url)
    }

    actual fun checkAuth() {}
}
