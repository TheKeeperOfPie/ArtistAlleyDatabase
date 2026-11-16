package com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare

import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.AuthToken
import com.thekeeperofpie.artistalleydatabase.alley.functions.auth.RefreshToken
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import org.w3c.fetch.Headers
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import kotlin.io.encoding.Base64
import kotlin.js.Promise

@Suppress("NOTHING_TO_INLINE")
internal inline fun fetch(r: Request, @Suppress("unused") param: Unit = Unit) = fetch(request = r)

private external fun fetch(request: Request): Promise<Response>

internal external val crypto: Crypto

internal external interface Crypto {
    fun getRandomValues(typedArray: Int8Array): Int8Array
}

internal fun Crypto.createAuthToken(): AuthToken = AuthToken(createToken())
internal fun Crypto.createRefreshToken(): RefreshToken = RefreshToken(createToken())

private fun Crypto.createToken() = Int8Array(64)
    .let(this::getRandomValues)
    .toByteArray()
    .let(Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)::encode)

private external fun decodeURIComponent(encodedURI: String): String

val Headers.cookies: Map<String, String?>
    get() {
        val header = get("Cookie")
        if (header.isNullOrBlank()) return emptyMap()
        return header.split(";")
            .associate {
                val pieces = it.split("=")
                val name = pieces.getOrNull(0)?.trim() ?: return@associate it to null
                val value = pieces.getOrNull(1)?.trim() ?: return@associate it to null
                name to decodeURIComponent(value)
            }
    }
