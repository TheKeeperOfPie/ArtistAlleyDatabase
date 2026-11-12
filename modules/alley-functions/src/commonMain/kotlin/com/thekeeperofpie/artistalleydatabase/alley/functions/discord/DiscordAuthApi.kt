package com.thekeeperofpie.artistalleydatabase.alley.functions.discord

import com.thekeeperofpie.artistalleydatabase.alley.functions.Env
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.fetch
import kotlinx.coroutines.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.dom.url.URLSearchParams
import org.w3c.fetch.FOLLOW
import org.w3c.fetch.Headers
import org.w3c.fetch.Request
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestRedirect

internal object DiscordAuthApi {
    private val json = Json {
        encodeDefaults = false
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun getAuth(env: Env, authCode: String): AuthTokenResponse {
        val result = fetch(
            Request(
                "https://discord.com/api/oauth2/token",
                RequestInit(
                    method = "POST",
                    headers = Headers().apply {
                        this.set("Authorization", "Bot ${env.DISCORD_TOKEN}")
                        this.set("Content-Type", "application/x-www-form-urlencoded")
                    },
                    body = URLSearchParams().apply {
                        set("client_id", env.DISCORD_APP_ID)
                        set("client_secret", env.DISCORD_CLIENT_SECRET)
                        set("grant_type", "authorization_code")
                        set("code", authCode)
                        set("redirect_uri", env.DISCORD_REDIRECT_URL)
                    },
                    cache = undefined,
                    integrity = undefined,
                    redirect = RequestRedirect.FOLLOW,
                )
            )
        ).await().text().await()
        return json.decodeFromString<AuthTokenResponse>(result)
    }

    suspend fun getUserId(accessToken: String): String? {
        val result = fetch(
            Request(
                "https://discord.com/api/v10/users/@me",
                RequestInit(
                    method = "GET",
                    headers = Headers().apply {
                        this.set("Authorization", "Bearer $accessToken")
                        this.set("Content-Type", "application/json")
                    },
                    cache = undefined,
                    integrity = undefined,
                    redirect = RequestRedirect.FOLLOW,
                )
            )
        ).await()
        if (!result.ok) return null
        val body = result.text().await()
        return json.decodeFromString<DiscordUser>(body).id
    }

    suspend fun revokeToken(env: Env, token: String, isRefreshToken: Boolean) {
        fetch(
            Request(
                "https://discord.com/api/oauth2/token/revoke",
                RequestInit(
                    method = "POST",
                    headers = Headers().apply {
                        this.set("Authorization", "Bot ${env.DISCORD_TOKEN}")
                        this.set("Content-Type", "application/x-www-form-urlencoded")
                    },
                    body = URLSearchParams().apply {
                        set("client_id", env.DISCORD_APP_ID)
                        set("client_secret", env.DISCORD_CLIENT_SECRET)
                        set("token", token)
                        set(
                            "token_type_hint",
                            if (isRefreshToken) "refresh_token" else "access_token"
                        )
                    },
                    cache = undefined,
                    integrity = undefined,
                    redirect = RequestRedirect.FOLLOW,
                )
            )
        ).await()
    }

    // TODO: Share this with the bot?
    @Serializable
    data class AuthTokenResponse(
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("refresh_token")
        val refreshToken: String,
    )

    @Serializable
    data class DiscordUser(
        val id: String?,
    )
}
