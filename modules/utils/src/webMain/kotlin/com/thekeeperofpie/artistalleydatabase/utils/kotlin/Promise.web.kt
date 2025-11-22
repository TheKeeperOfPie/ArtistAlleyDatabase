package com.thekeeperofpie.artistalleydatabase.utils.kotlin

import kotlin.js.JsAny
import kotlin.js.Promise

// TODO: Official support pending at https://github.com/Kotlin/kotlinx.coroutines/issues/4544
expect suspend inline fun <reified T : JsAny?> Promise<T>.await(): T
