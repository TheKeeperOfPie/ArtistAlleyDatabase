@file:JsModule("cloudflare:workers")
package com.thekeeperofpie.artistalleydatabase.cloudflare

import org.w3c.fetch.Request
import org.w3c.fetch.Response
import kotlin.js.Promise

abstract external class DurableObject(state: DurableObjectState, env: dynamic) {
    val ctx: DurableObjectState
    val env: dynamic
    open fun fetch(request: Request): Promise<Response>

    open fun webSocketMessage(ws: CloudflareWebSocket, message: Any)
    open fun webSocketClose(ws: CloudflareWebSocket, code: Int, reason: String, wasClean: Boolean)
    open fun webSocketError(ws: CloudflareWebSocket, error: Any)
}

external interface DurableObjectState {
    fun acceptWebSocket(webSocket: CloudflareWebSocket, tags: Array<String> = definedExternally)
    fun getWebSockets(tag: String = definedExternally): Array<CloudflareWebSocket>
    fun setWebSocketAutoResponse(pair: WebSocketRequestResponsePair?)
}

external interface DurableObjectNamespace {
    fun getByName(name: String): DurableObjectStub
    fun idFromName(name: String): DurableObjectId
    fun get(id: DurableObjectId): DurableObjectStub
}

external interface DurableObjectStub {
    fun fetch(request: Request): Promise<Response>
    fun fetch(url: String, init: dynamic = definedExternally): Promise<Response>
}

external interface DurableObjectId
