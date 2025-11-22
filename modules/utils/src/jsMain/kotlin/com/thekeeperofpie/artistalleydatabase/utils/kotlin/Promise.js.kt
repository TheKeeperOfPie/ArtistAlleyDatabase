package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import kotlin.js.Promise
import kotlinx.coroutines.await as kotlinAwait

actual suspend inline fun <T : JsAny?> Promise<T>.await(): T = this.kotlinAwait()
