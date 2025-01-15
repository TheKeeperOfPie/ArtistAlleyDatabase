package com.thekeeperofpie.artistalleydatabase.alley

actual object PlatformSpecificConfig {
    actual val type = PlatformType.WASM
    actual val defaultPageSize = 50
    actual val showPagingButtons = true
}
