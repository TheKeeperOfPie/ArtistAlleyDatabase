package com.thekeeperofpie.artistalleydatabase.cloudflare

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.js.Promise

fun <T> promise(block: suspend () -> T) =
    Promise { resolve, reject ->
        block.startCoroutine(completion = object : Continuation<T> {
            override val context: CoroutineContext = EmptyCoroutineContext
            override fun resumeWith(result: Result<T>) {
                result.fold(resolve, reject)
            }
        })
    }
