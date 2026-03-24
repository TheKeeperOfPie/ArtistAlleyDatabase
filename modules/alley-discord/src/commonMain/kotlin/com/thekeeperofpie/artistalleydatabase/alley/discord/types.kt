package com.thekeeperofpie.artistalleydatabase.alley.discord

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
}

external interface Env {
    val DISCORD_BOT_APP_ID: String
    val DISCORD_BOT_PUBLIC_KEY: String
    val DISCORD_BOT_TOKEN: String
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
