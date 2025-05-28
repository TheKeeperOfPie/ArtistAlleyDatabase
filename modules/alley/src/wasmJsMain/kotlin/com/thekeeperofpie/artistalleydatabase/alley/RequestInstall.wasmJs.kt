package com.thekeeperofpie.artistalleydatabase.alley

import kotlinx.browser.window
import org.w3c.dom.get
import kotlin.js.Promise

internal external class BeforeInstallPromptEvent : JsAny {
    fun prompt(): Promise<*>
}

actual object RequestInstall {
    actual fun requestInstall() {
        window["deferredInstallPrompt"]?.unsafeCast<BeforeInstallPromptEvent>()?.prompt()
    }
}
