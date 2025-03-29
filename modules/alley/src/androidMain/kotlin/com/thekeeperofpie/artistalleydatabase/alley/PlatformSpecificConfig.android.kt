package com.thekeeperofpie.artistalleydatabase.alley

actual object PlatformSpecificConfig {
    actual val type = PlatformType.ANDROID
    actual val defaultPageSize = 25
    actual val showPagingButtons = false
    actual val scrollbarsAlwaysVisible = false
    actual val installable = false
    actual fun requestInstall(): Unit = throw UnsupportedOperationException()
}
