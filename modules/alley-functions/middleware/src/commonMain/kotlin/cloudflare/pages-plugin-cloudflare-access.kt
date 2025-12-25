package cloudflare

import org.w3c.fetch.Request
import org.w3c.fetch.Response
import kotlin.js.Promise

@JsModule("@cloudflare/pages-plugin-cloudflare-access")
external fun onRequest(pluginData: dynamic): (context: EventContext<Env>) -> Promise<Response>

@Suppress("NOTHING_TO_INLINE")
inline fun PluginArgs(domain: String? = undefined, aud: String? = undefined): dynamic {
    val o = js("({})")
    o["domain"] = domain
    o["aud"] = aud
    return o
}

external interface EventContext<Env> {
    val request: Request
    val env: Env
    val functionPath: String?
    fun next(request: Request): Promise<Response>
}

external interface Env {
    val IS_PRODUCTION: Boolean
}
