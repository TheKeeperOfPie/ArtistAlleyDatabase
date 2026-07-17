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
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.LastViewedPage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.dom.url.URL
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import kotlin.js.Promise
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.uuid.Uuid

private const val MAX_PAGES_PER_USER = 20
private val MAX_PAGE_AGE = 1.hours

@JsExport
class LastViewedServer(state: DurableObjectState, env: Env) : DurableObject(state, env) {

    private var pageHistory = mutableMapOf<String, Map<LastViewedPage, Instant>>()
    private val json = Json { allowStructuredMapKeys = true }

    init {
        ctx.blockConcurrencyWhile {
            ctx.storage.get("history").then<Unit>(
                onFulfilled = {
                    if (it != null) {
                        try {
                            val saved =
                                json.decodeFromString<Map<String, Map<LastViewedPage, Instant>>>(it.toString())
                            saved.forEach { (identifier, history) ->
                                pageHistory[identifier] =
                                    pageHistory[identifier].orEmpty() + history
                            }
                            sendSync()
                        } catch (t: Throwable) {
                            console.log("Failed to load history: ${t.message}")
                            t.printStackTrace()
                        }
                    }
                },
                onRejected = {
                    console.log("Failed to load history from storage")
                    it.printStackTrace()
                }
            )
        }

        ctx.setWebSocketAutoResponse(WebSocketRequestResponsePair("ping", "pong"))
    }

    override fun fetch(request: Request): Promise<Response> = promise {
        val webSocketPair = WebSocketPair()
        val client = webSocketPair.client
        val server = webSocketPair.server

        val instanceId = try {
            URL(request.url).searchParams.get("instanceId")?.let(Uuid::parse)
        } catch (_: Throwable) {
            null
        } ?: Uuid.random()

        ctx.acceptWebSocket(server)

        val identifier = request.headers.get("Cf-Access-Authenticated-User-Email")
            ?: instanceId.toString()
        val attachment = Attachment(sessionId = instanceId, identifier = identifier)
        server.serializeAttachment(json.encodeToString(attachment))

        sendSync()

        val responseInit = js("{}").unsafeCast<CloudflareResponseInit>()
        responseInit.status = 101
        responseInit.webSocket = client
        Response(null, responseInit)
    }

    override fun webSocketMessage(ws: CloudflareWebSocket, message: Any) {
        val identifier = ws.getIdentifier() ?: return
        try {
            val event = json.decodeFromString<LastViewedEvent>(message.toString())
            console.log("Received LastViewedEvent $event")
            when (val event = event) {
                is LastViewedEvent.Update -> {
                    val now = Clock.System.now()
                    pageHistory[identifier] = pageHistory[identifier].orEmpty()
                        .plus(event.page to now)

                    pruneUserHistory(identifier, now)
                    saveHistory()
                    sendSync()
                }
                is LastViewedEvent.Debug,
                is LastViewedEvent.Sync,
                    -> Unit
            }
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
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        sendSync(caller = ws)
    }

    override fun webSocketError(ws: CloudflareWebSocket, error: Any) {
        sendSync(caller = ws)
    }

    private fun sendSync(caller: CloudflareWebSocket? = null) {
        val now = Clock.System.now()

        pageHistory.keys.toList().forEach {
            pruneUserHistory(it, now)
        }

        val usersToVisits = pageHistory
            .mapValues { (_, history) ->
                history.entries
                    .sortedByDescending { it.value }
                    .take(MAX_PAGES_PER_USER)
                    .map { LastViewedEvent.Sync.PageVisit(it.key, it.value) }
            }

        ctx.getWebSockets().forEach { webSocket ->
            if (webSocket == caller) return@forEach
            val identifier = webSocket.getIdentifier() ?: return@forEach
            val filteredMap = usersToVisits - identifier
            val event = json.encodeToString<LastViewedEvent>(LastViewedEvent.Sync(filteredMap))
            try {
                webSocket.send(event)
            } catch (t: Throwable) {
                console.log("Failed to sync to socket $webSocket")
                t.printStackTrace()
            }
        }
    }

    private fun pruneUserHistory(identifier: String, now: Instant) {
        val history = pageHistory[identifier] ?: return
        val newHistory = history
            .entries
            .filter { it.value > (now - MAX_PAGE_AGE) }
            .sortedByDescending { it.value }
            .take(MAX_PAGES_PER_USER)
            .associate { it.key to it.value }
        if (newHistory.isEmpty()) {
            pageHistory.remove(identifier)
        } else {
            pageHistory[identifier] = newHistory
        }
    }

    private fun saveHistory() {
        ctx.storage.put("history", json.encodeToString(pageHistory))
    }

    private fun CloudflareWebSocket.getIdentifier(): String? {
        val attachment = deserializeAttachment()?.toString() ?: return null
        return try {
            json.decodeFromString<Attachment>(attachment).identifier
        } catch (_: Throwable) {
            null
        }
    }
}

@Serializable
private data class Attachment(
    val sessionId: Uuid,
    val identifier: String,
)
