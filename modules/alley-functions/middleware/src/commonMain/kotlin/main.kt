@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class)

import cloudflare.Env
import cloudflare.EventContext
import cloudflare.PluginArgs
import com.thekeeperofpie.artistalleydatabase.alley.functions.middleware.secrets.BuildKonfig
import org.w3c.dom.url.URL
import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.js.Promise

@JsExport
class Middleware {
    companion object {
        @JsStatic
        fun request(context: EventContext<Env>): Promise<Response> {
            val pathSegments = URL(context.request.url).pathname.split("/")
            return if (BuildKonfig.debug) {
                context.next(context.request)
            } else {
                val firstSegment = pathSegments.getOrNull(1)
                val secondSegment = pathSegments.getOrNull(2)
                when (firstSegment) {
                    "user" if secondSegment == "api" -> context.next(context.request)
                    "form" if secondSegment == "api" -> context.next(context.request)
                    "edit" if secondSegment == "api" ->
                        cloudflare.onRequest(
                            pluginData = PluginArgs(
                                domain = BuildKonfig.cloudflareAccessDomain,
                                aud = BuildKonfig.cloudflareAccessEditAudienceTag,
                            )
                        )(context)
                    else -> promise { Response("", ResponseInit(status = 404)) }
                }
            }
        }

        private fun <T> promise(block: suspend () -> T) =
            Promise { resolve, reject ->
                block.startCoroutine(completion = object : Continuation<T> {
                    override val context: CoroutineContext = EmptyCoroutineContext
                    override fun resumeWith(result: Result<T>) {
                        result.fold(resolve, reject)
                    }
                })
            }
    }
}
