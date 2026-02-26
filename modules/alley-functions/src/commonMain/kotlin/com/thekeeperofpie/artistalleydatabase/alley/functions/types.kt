@file:OptIn(ExperimentalWasmJsInterop::class, ExperimentalJsCollectionsApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.functions

import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.CloudflareAccessPlugin
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.D1Database
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.KeyValueStore
import com.thekeeperofpie.artistalleydatabase.alley.functions.cloudflare.R2Bucket
import org.w3c.fetch.Request
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.js.Promise

external interface EventContext {
    val request: Request
    val env: Env
    val functionPath: String?
    val data: PluginData?
}

external interface PluginData {
    val cloudflareAccess: CloudflareAccessPlugin?
}

external interface Env {
    val ARTIST_ALLEY_DB: D1Database
    val ARTIST_ALLEY_FORM_DB: D1Database
    val ARTIST_ALLEY_IMAGES_BUCKET: R2Bucket
    val ARTIST_ALLEY_CACHE_KV: KeyValueStore
    val IMAGES_ACCESS_KEY_ID: String
    val IMAGES_SECRET_ACCESS_KEY_ID: String
    val IMAGES_CLOUDFLARE_URL: String
}

internal fun <T> promise(block: suspend () -> T) =
    Promise { resolve, reject ->
        block.startCoroutine(completion = object : Continuation<T> {
            override val context: CoroutineContext = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) {
                result.fold(resolve, reject)
            }
        })
    }
