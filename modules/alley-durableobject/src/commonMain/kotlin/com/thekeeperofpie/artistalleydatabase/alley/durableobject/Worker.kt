@file:OptIn(ExperimentalJsExport::class, ExperimentalJsStatic::class, ExperimentalStdlibApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.durableobject

import com.thekeeperofpie.artistalleydatabase.cloudflare.promise
import kotlinx.coroutines.await
import org.w3c.fetch.Request
import org.w3c.fetch.Response
import kotlin.js.Promise

@JsExport
fun handleRequest(request: Request, env: Env): Promise<Response> {
    console.log("Entering handleRequest")
    return promise {
        console.log("Inside promise block")
        try {
            console.log("Received request inside durableobject Worker: ${request.url}")
            val stub = env.LAST_VIEWED_SERVER.getByName("lastViewed")
            console.log("Got stub: $stub")
            val responsePromise = stub.fetch(request)
            console.log("Called stub.fetch, got promise: $responsePromise")
            val response = responsePromise.await()
            console.log("Awaited response: $response")
            response
        } catch (e: Throwable) {
            console.log("Error in handleRequest: ${e.message}")
            e.printStackTrace()
            Response("Internal Server Error: ${e.message}", org.w3c.fetch.ResponseInit(status = 500))
        }
    }
}
