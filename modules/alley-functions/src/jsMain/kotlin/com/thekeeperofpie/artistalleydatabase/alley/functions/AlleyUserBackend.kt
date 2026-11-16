package com.thekeeperofpie.artistalleydatabase.alley.functions

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.AuthToken
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.RefreshToken
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.cookies
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.createAuthToken
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.createRefreshToken
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.crypto
import com.thekeeperofpie.artistalleydatabase.alley.functions.discord.DiscordAuthApi
import com.thekeeperofpie.artistalleydatabase.alley.models.AlleyAuthorization
import com.thekeeperofpie.artistalleydatabase.alley.user.AuthCode
import com.thekeeperofpie.artistalleydatabase.alley.user.User
import com.thekeeperofpie.artistalleydatabase.alley.user.UserSession
import kotlinx.coroutines.await
import org.w3c.dom.url.URL
import org.w3c.fetch.Headers
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.time.Clock

internal object AlleyUserBackend {

    private val forbidden
        get() = Response("", ResponseInit(status = 403, headers = Headers().apply {
            attachAuthToken(null)
        }))

    private val unauthorized
        get() = Response("", ResponseInit(status = 403, headers = Headers().apply {
            attachRefreshToken(null)
        }))

    suspend fun handleRequest(context: EventContext, path: String): Response {
        val pathSegments = path.removePrefix("/").split("/")
        val firstSegment = pathSegments.getOrNull(0)

        return when (firstSegment) {
            "login" -> handleLogin(context, context.request)
            "refresh" -> handleAuthRefresh(context, context.request)
            "auth" -> handleAuthCode(context, context.request)
            "check" -> checkAuth(context, context.request)
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

        Databases.userDatabase(context).authCodeQueries.insertCode(AuthCode(state, userId))

        return Response("", ResponseInit(status = 302, headers = Headers().apply {
            // TODO: Redirect back to exact caller page?
            set("Location", url.origin)
        }))
    }

    private suspend fun handleAuthCode(context: EventContext, request: Request): Response {
        val authCode = request.text().await()
        val database = Databases.userDatabase(context)

        // TODO: Hash
        val userId = database.authCodeQueries.consumeCode(authCode).awaitAsOneOrNull()
            ?: return Response("", ResponseInit(status = 403))
        val refreshToken = crypto.createRefreshToken()
        database.userQueries.provisionUser(
            User(
                userId = userId,
                refreshToken = refreshToken,
                createdAt = Clock.System.now(),
                deviceIdentifier = request.headers.get("User-Agent"),
            )
        )

        val authToken = crypto.createAuthToken()
        database.userSessionQueries.createSession(
            UserSession(
                userId = userId,
                authToken = authToken,
                createdAt = Clock.System.now(),
            )
        )

        return Response("", ResponseInit(status = 200, headers = Headers().apply {
            attachRefreshToken(refreshToken)
            attachAuthToken(authToken)
        }))
    }

    private suspend fun handleAuthRefresh(context: EventContext, request: Request): Response {
        // TODO: Pass the userId and only check that row
        val database = Databases.userDatabase(context)
        val cookies = request.headers.cookies
        val refreshToken = cookies[AlleyAuthorization.COOKIE_REFRESH_TOKEN]
            ?.let(::RefreshToken)
            ?: return unauthorized

        val userId = database.userQueries.checkRefreshToken(refreshToken).awaitAsOneOrNull()
            ?: return unauthorized

        val oldAuthToken = cookies[AlleyAuthorization.COOKIE_AUTH_TOKEN]
        if (oldAuthToken != null) {
            database.userSessionQueries.revokeAuthToken(userId, AuthToken(oldAuthToken))
        }

        val newAuthToken = crypto.createAuthToken()
        database.userSessionQueries.createSession(
            UserSession(
                userId = userId,
                authToken = newAuthToken,
                createdAt = Clock.System.now(),
            )
        )

        return Response("", ResponseInit(status = 200, headers = Headers().apply {
            attachAuthToken(newAuthToken)
        }))
    }

    private suspend fun checkAuth(context: EventContext, request: Request): Response {
        val database = Databases.userDatabase(context)
        val authToken = request.headers.cookies[AlleyAuthorization.COOKIE_AUTH_TOKEN]
            ?: return forbidden
        database.userSessionQueries.checkAuthToken(AuthToken(authToken)).awaitAsOneOrNull()
            ?: return forbidden
        // TODO: Time based expiry
        return Response("", ResponseInit(status = 200, headers = Headers()))
    }

    private fun Headers.attachRefreshToken(refreshToken: RefreshToken?) {
        if (refreshToken == null) {
            attachAuthToken(null)
        }

        val maxAge = if (refreshToken == null) 0 else 31536000
        append(
            "Set-Cookie",
            "${AlleyAuthorization.COOKIE_REFRESH_TOKEN}=${refreshToken?.token.orEmpty()}; Max-Age=$maxAge; Path=/user/api/refresh; Secure; HttpOnly; SameSite=Strict"
        )
        val authorized = if (refreshToken == null) 0 else 1
        append(
            "Set-Cookie",
            "${AlleyAuthorization.COOKIE_INSECURE_AUTHORIZED}=$authorized; Path=/; Max-Age=$maxAge; Secure; SameSite=Strict"
        )

    }

    private fun Headers.attachAuthToken(authToken: AuthToken?) {
        val maxAge = if (authToken == null) 0 else 86400
        append(
            "Set-Cookie",
            "${AlleyAuthorization.COOKIE_AUTH_TOKEN}=${authToken?.token.orEmpty()}; Max-Age=$maxAge; Path=/user/api; Secure; HttpOnly; SameSite=Strict"
        )
    }
}
