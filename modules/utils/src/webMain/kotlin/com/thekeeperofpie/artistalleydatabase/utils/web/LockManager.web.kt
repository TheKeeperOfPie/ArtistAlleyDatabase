package com.thekeeperofpie.artistalleydatabase.utils.web

import kotlin.js.Promise

external interface LockManager {
    fun <T : JsAny?> request(name: String, callback: (lock: Lock?) -> Promise<T>): Promise<T>
}

external interface Lock
