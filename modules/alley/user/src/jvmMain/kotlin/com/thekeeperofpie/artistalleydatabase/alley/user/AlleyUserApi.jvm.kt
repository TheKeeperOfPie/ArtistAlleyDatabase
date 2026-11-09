package com.thekeeperofpie.artistalleydatabase.alley.user

actual class AlleyUserApi {
    actual suspend fun exchangeAuthCode(authCode: String): String? = null
}
