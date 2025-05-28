package com.thekeeperofpie.artistalleydatabase.alley.utils

import com.thekeeperofpie.artistalleydatabase.alley.ConsoleLogger
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.Navigator
import kotlin.js.Promise

private external object StorageManager : JsAny {
    fun persist(): Promise<JsBoolean>
    fun persisted(): Promise<JsBoolean>
}

private fun storage(): StorageManager? = js("navigator.storage")

private val Navigator.storage: StorageManager?
    get() = storage()

actual object PersistentStorageRequester {
    actual suspend fun requestPersistent() {
        try {
            val storageManager = window.navigator.storage!!
            val wasPersisted = storageManager.persisted().await<JsBoolean>().toBoolean()
            if (!wasPersisted) {
                val persisted = storageManager.persist().await<JsBoolean>().toBoolean()
                ConsoleLogger.log("Persistent storage requested: $persisted")
            }
        } catch (throwable: Throwable) {
            ConsoleLogger.log("Failed to request persistent storage: ${throwable.message}")
        }
    }
}
