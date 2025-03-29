package com.thekeeperofpie.artistalleydatabase.alley

actual object PlatformSpecificConfig {
    actual val type = PlatformType.DESKTOP
    actual val defaultPageSize = 50
    actual val showPagingButtons = true
    actual val scrollbarsAlwaysVisible = true
    actual val installable = false
    actual fun requestInstall(): Unit = throw UnsupportedOperationException()
}
