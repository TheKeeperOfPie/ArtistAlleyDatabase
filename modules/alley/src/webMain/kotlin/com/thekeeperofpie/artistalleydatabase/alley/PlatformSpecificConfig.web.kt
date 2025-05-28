package com.thekeeperofpie.artistalleydatabase.alley

import kotlinx.browser.window
import org.w3c.dom.get

actual object PlatformSpecificConfig {
    actual val type = PlatformType.WASM
    actual val defaultPageSize = 50
    actual val showPagingButtons = true
    actual val scrollbarsAlwaysVisible = true
    actual val installable
        get() = !window.matchMedia("(display-mode: standalone)").matches &&
                !window.matchMedia("(display-mode: fullscreen)").matches &&
                !window.matchMedia("(display-mode: minimal-ui)").matches &&
                window["deferredInstallPrompt"] != null

    actual fun requestInstall() = RequestInstall.requestInstall()
}
