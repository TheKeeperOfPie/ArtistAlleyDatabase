package com.thekeeperofpie.artistalleydatabase.alley.user

expect class AlleyUserApi {
    suspend fun exchangeAuthCode(authCode: String): String?
}
