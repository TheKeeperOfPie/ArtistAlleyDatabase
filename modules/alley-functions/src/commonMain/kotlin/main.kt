@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class)

import org.w3c.fetch.Headers
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.js.Promise

@JsExport
class Worker {
    companion object {
        @JsStatic
        fun request(context: EventContext<Env>): Response {
            val headers = Headers().apply {
                set("Content-Type", "text/plain")
            }
            val body = "TODO"
            return Response(body, ResponseInit(headers = headers))
        }
    }
}

external interface EventContext<Env> {
    var request: Request
    var waitUntil: (promise: Promise<Any>) -> Unit
    var env: Env
    var params: String?
}

external interface Env {
    val ANIME_EXPO_2026_DB: D1Database
}

external interface D1Database
