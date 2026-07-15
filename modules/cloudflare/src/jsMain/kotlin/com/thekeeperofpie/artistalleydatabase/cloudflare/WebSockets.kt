package com.thekeeperofpie.artistalleydatabase.cloudflare

import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.fetch.ResponseInit

external class WebSocketPair {
    @JsName("0") val client: CloudflareWebSocket
    @JsName("1") val server: CloudflareWebSocket
}

external class CloudflareWebSocket {
    fun accept()
    fun addEventListener(type: String, callback: EventListener)
    fun send(message: Any)
    fun close(code: Int = definedExternally, reason: String = definedExternally)

    fun serializeAttachment(attachment: Any?)
    fun deserializeAttachment(): Any?
}

external class WebSocketEvent : Event {
    val data: Any?
}

external interface CloudflareResponseInit : ResponseInit {
    var webSocket: CloudflareWebSocket?
}

external class WebSocketRequestResponsePair(request: String, response: String)
