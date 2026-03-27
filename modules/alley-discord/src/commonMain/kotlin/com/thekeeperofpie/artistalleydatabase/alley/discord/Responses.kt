package com.thekeeperofpie.artistalleydatabase.alley.discord

import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit

internal object Responses {
    val response401
        get() = Response(
            "Bad signature",
            ResponseInit(status = 401, headers = Headers().apply {
                set("Content-Type", "text/plain")
            })
        )
    val response404
        get() = Response(
            "Failed to resolve request",
            ResponseInit(status = 404, headers = Headers().apply {
                set("Content-Type", "text/plain")
            })
        )
    val response200 get() = Response(null, ResponseInit(status = 200, headers = Headers()))
    val response202 get() = Response(null, ResponseInit(status = 202, headers = Headers()))
    val responseReturnToDiscord
        get() = Response(
            """
                <!DOCTYPE html>
                <html lang="en">
                  <head>
                    <meta charset="utf-8">
                    <meta name="color-scheme" content="dark light" />
                    <title>Artist Alley Directory Verification</title>
                  </head>
                  <body>
                    <h1>Please return to Discord to see the verification result</h1>
                  </body>
                </html>
            """.trimIndent(),
            ResponseInit(
                status = 202,
                headers = Headers().apply {
                    set("Content-Type", "text/html")
                },
            )
        )
}

internal inline fun <reified T> jsonResponse(value: T) = Response(
    body = Json.encodeToString(value),
    init = ResponseInit(status = 200, headers = Headers().apply {
        set("Content-Type", "application/json")
    })
)
