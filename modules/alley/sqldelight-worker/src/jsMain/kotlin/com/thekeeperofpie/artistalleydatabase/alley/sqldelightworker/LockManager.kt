package com.thekeeperofpie.artistalleydatabase.alley.sqldelightworker

import kotlin.js.Promise

external interface LockManager {
    fun <T> request(name: String, callback: (lock: Lock?) -> Promise<T>): Promise<T>
}

external interface Lock
