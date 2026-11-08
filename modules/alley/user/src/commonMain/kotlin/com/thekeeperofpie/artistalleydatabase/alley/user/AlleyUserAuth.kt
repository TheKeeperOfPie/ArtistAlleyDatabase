package com.thekeeperofpie.artistalleydatabase.alley.user

expect class AlleyUserAuth {

    var isLoggedIn: Boolean
        private set
}

@Suppress("UnusedReceiverParameter")
internal val AlleyUserAuth.COOKIE_CLIENT_AUTH_SIGNAL get() = "AADB_Authorized"
