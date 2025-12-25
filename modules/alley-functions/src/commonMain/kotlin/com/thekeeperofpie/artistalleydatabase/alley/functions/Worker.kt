@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class)

package com.thekeeperofpie.artistalleydatabase.alley.functions

import org.w3c.fetch.Response
import org.w3c.fetch.ResponseInit
import kotlin.js.Promise

@JsExport
class Worker {
    companion object {
        @JsStatic
        fun request(context: EventContext): Promise<Response> = promise {
            val functionPath = context.functionPath.orEmpty()
            when {
                // TODO: Use non-prefixed path?
                functionPath.startsWith("/form/api") -> AlleyFormBackend.handleRequest(context)
                functionPath.startsWith("/edit/api") -> AlleyEditBackend.handleRequest(
                    context = context,
                    path = functionPath.removePrefix("/edit/api"),
                )
                else -> Response("", ResponseInit(status = 404))
            }
        }
    }
}
