package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.user.AuthCode
import kotlinx.coroutines.await
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.dom.url.URL
import org.w3c.dom.url.URLSearchParams
import org.w3c.fetch.FOLLOW
import org.w3c.fetch.Headers
import org.w3c.fetch.Request
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestRedirect
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.js.Promise

internal object AlleyUserBackend {

    suspend fun handleRequest(context: EventContext, path: String): Response {
        val pathSegments = path.removePrefix("/").split("/")
        val firstSegment = pathSegments.getOrNull(0)

        return when (firstSegment) {
            "login" -> handleLogin(context, context.request)
            "auth" -> handleAuthCode(context, context.request)
            else -> Response("", ResponseInit(status = 404))
        }
    }

    private suspend fun handleLogin(context: EventContext, request: Request): Response {
        val url = URL(request.url)
        val params = url.searchParams
        val code = params.get("code") ?: return Response("", ResponseInit(status = 404))
        val state = params.get("state") ?: return Response("", ResponseInit(status = 404))
        val authResponse = DiscordAuthApi.getAuth(context.env, code)

        val userId = try {
            DiscordAuthApi.getUserId(authResponse.accessToken)
        } finally {
            try {
                DiscordAuthApi.revokeToken(
                    context.env,
                    authResponse.accessToken,
                    isRefreshToken = false
                )
            } finally {
                DiscordAuthApi.revokeToken(
                    context.env,
                    authResponse.accessToken,
                    isRefreshToken = true
                )
            }
        } ?: return Response("", ResponseInit(status = 404))

        Databases.userDatabase(context).authCodeQueries.insertUserId(AuthCode(state, userId))

        return Response("", ResponseInit(status = 302, headers = Headers().apply {
            // TODO: Redirect back to exact caller page?
            set("Location", url.origin)
        }))
    }

    private suspend fun handleAuthCode(context: EventContext, request: Request): Response {
        val authCode = request.text().await()
        val userId =
            Databases.userDatabase(context).authCodeQueries.getUserId(authCode).awaitAsOneOrNull()
                ?: return Response("", ResponseInit(status = 403))

        return Response("", ResponseInit(status = 200, headers = Headers().apply {
            append(
                "Set-Cookie",
                "AADB_Authorization=$userId; Max-Age=31536000; Path=/user/api; Secure; HttpOnly; SameSite=Strict"
            )
            append(
                "Set-Cookie",
                "AADB_Authorized=1; Path=/; Max-Age=31536000; Secure; SameSite=Strict"
            )
        }))
    }

    private object DiscordAuthApi {
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
            return json.decodeFromString<User>(body).id
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
    }

    // TODO: Share this with the bot?
    @Serializable
    private data class AuthTokenResponse(
        @SerialName("access_token")
        val accessToken: String,
        @SerialName("refresh_token")
        val refreshToken: String,
    )

    @Serializable
    private data class User(
        val id: String?,
    )
}

private external fun fetch(request: Request): Promise<Response>
