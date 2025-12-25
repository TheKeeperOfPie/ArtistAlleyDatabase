@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class)

import cloudflare.Env
import cloudflare.EventContext
import cloudflare.PluginArgs
import com.thekeeperofpie.artistalleydatabase.alley.functions.middleware.secrets.BuildKonfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.js.Promise

@JsExport
class Middleware {
    companion object {
        @JsStatic
        fun request(@Suppress("unused") context: EventContext<Env>): Promise<Response> {
            val functionPath = context.functionPath.orEmpty()
            console.log("requested $functionPath")
            return if (BuildKonfig.debug) {
                context.next(context.request)
            } else when {
                functionPath.startsWith("/form/api") -> cloudflare.onRequest(
                    pluginData = PluginArgs(
                        domain = BuildKonfig.cloudflareAccessDomain,
                        aud = BuildKonfig.cloudflareAccessFormAudienceTag,
                    )
                )(context)
                functionPath.startsWith("/edit/api") -> cloudflare.onRequest(
                    pluginData = PluginArgs(
                        domain = BuildKonfig.cloudflareAccessDomain,
                        aud = BuildKonfig.cloudflareAccessEditAudienceTag,
                    )
                )(context)
                else -> promise { Response("", ResponseInit(status = 404)) }
            }
        }

        private fun <T> promise(block: suspend CoroutineScope.() -> T) =
            Promise { resolve, reject ->
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch {
                    try {
                        resolve(block())
                    } catch (t: Throwable) {
                        reject(t)
                    }
                }
            }
    }
}
