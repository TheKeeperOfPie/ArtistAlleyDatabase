@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class)

import cloudflare.Env
import cloudflare.EventContext
import cloudflare.PluginArgs
import com.thekeeperofpie.artistalleydatabase.alley.functions.middleware.secrets.BuildKonfig
import org.w3c.fetch.Response
import kotlin.js.Promise

@JsExport
class Middleware {
    companion object {
        @JsStatic
        fun request(@Suppress("unused") context: EventContext<Env>): Promise<Response> =
            if (context.env.IS_PRODUCTION) {
                cloudflare.onRequest(
                    pluginData = PluginArgs(
                        domain = BuildKonfig.cloudflareAccessDomain,
                        aud = BuildKonfig.cloudflareAccessAudienceTag,
                    )
                )(context)
            } else {
                context.next(context.request)
            }
    }
}
