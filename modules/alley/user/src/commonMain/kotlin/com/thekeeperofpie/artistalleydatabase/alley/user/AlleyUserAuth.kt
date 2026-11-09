package com.thekeeperofpie.artistalleydatabase.alley.user

import androidx.compose.ui.platform.UriHandler

expect class AlleyUserAuth {

    var isLoggedIn: Boolean
        private set

    fun startAuth(uriHandler: UriHandler, url: String)
}

@Suppress("UnusedReceiverParameter")
internal val AlleyUserAuth.COOKIE_CLIENT_AUTH_SIGNAL get() = "AADB_Authorized"
