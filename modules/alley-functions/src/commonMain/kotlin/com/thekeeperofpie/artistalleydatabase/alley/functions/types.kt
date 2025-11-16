@file:OptIn(ExperimentalWasmJsInterop::class, ExperimentalJsCollectionsApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.functions

import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.D1Database
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.R2Bucket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.fetch.Request
import kotlin.js.Promise

external interface EventContext {
    val request: Request
    val waitUntil: (promise: Promise<Any>) -> Unit
    val env: Env
    val functionPath: String?
}

external interface Env {
    val ARTIST_ALLEY_DB: D1Database
    val ARTIST_ALLEY_IMAGES_BUCKET: R2Bucket
}

internal fun <T> promise(block: suspend CoroutineScope.() -> T) =
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
