@file:OptIn(ExperimentalJsExport::class)

package com.thekeeperofpie.artistalleydatabase.alley.durableobject

import com.thekeeperofpie.artistalleydatabase.cloudflare.CloudflareResponseInit
import com.thekeeperofpie.artistalleydatabase.cloudflare.CloudflareWebSocket
import com.thekeeperofpie.artistalleydatabase.cloudflare.DurableObject
import com.thekeeperofpie.artistalleydatabase.cloudflare.DurableObjectState
import com.thekeeperofpie.artistalleydatabase.cloudflare.WebSocketPair
import com.thekeeperofpie.artistalleydatabase.cloudflare.WebSocketRequestResponsePair
import com.thekeeperofpie.artistalleydatabase.cloudflare.promise
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LastViewedEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import kotlin.js.Promise
import kotlin.uuid.Uuid

@JsExport
class LastViewedServer(state: DurableObjectState, env: Env) : DurableObject(state, env) {

    private val sessions = mutableMapOf<CloudflareWebSocket, Attachment>()

    init {
        ctx.getWebSockets().forEach { webSocket ->
            val attachment = webSocket.deserializeAttachment()
            if (attachment != null) {
                try {
                    sessions[webSocket] = Json.decodeFromString<Attachment>(attachment.toString())
                } catch (t: Throwable) {
                    console.log("Failed to deserialize attachment: ${t.message}")
                    t.printStackTrace()
                }
            }
        }

        ctx.setWebSocketAutoResponse(WebSocketRequestResponsePair("ping", "pong"))
    }

    override fun fetch(request: Request): Promise<Response> = promise {
        val webSocketPair = WebSocketPair()
        val client = webSocketPair.client
        val server = webSocketPair.server

        ctx.acceptWebSocket(server)

        val attachment = Attachment(sessionId = Uuid.random())
        server.serializeAttachment(Json.encodeToString(attachment))

        sessions[server] = attachment

        val responseInit = js("{}").unsafeCast<CloudflareResponseInit>()
        responseInit.status = 101
        responseInit.webSocket = client
        Response(null, responseInit)
    }

    override fun webSocketMessage(ws: CloudflareWebSocket, message: Any) {
        console.log("webSocketMessage received: $message")
        val session = sessions[ws]
        val sessionId = session?.sessionId

        try {
            val event = Json.decodeFromString<LastViewedEvent>(message.toString())
            console.log("Received LastViewedEvent: $event from $sessionId")
        } catch (t: Throwable) {
            console.log("Failed to deserialize message $message")
            t.printStackTrace()
        }
    }

    override fun webSocketClose(
        ws: CloudflareWebSocket,
        code: Int,
        reason: String,
        wasClean: Boolean,
    ) {
        try {
            ws.close(code, reason)
            sessions.remove(ws)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun CloudflareWebSocket.sendEvent(event: LastViewedEvent) {
        send(Json.encodeToString(event))
    }
}

@Serializable
private data class Attachment(
    val sessionId: Uuid,
)
