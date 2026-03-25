package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AuthTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
)
