package com.thekeeperofpie.artistalleydatabase.alley.functions

import kotlinx.serialization.json.Json
import org.w3c.fetch.Headers
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit

internal object Utils {

    val unauthorizedResponse = Response(null, ResponseInit(status = 403))

    inline fun <reified T> jsonResponse(value: T) = Response(
        body = Json.encodeToString(value),
        init = ResponseInit(status = 200, headers = Headers().apply {
            set("Content-Type", "application/json")
        })
    )
}
