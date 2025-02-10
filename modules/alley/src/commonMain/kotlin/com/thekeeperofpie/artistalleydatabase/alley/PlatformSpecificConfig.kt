package com.thekeeperofpie.artistalleydatabase.alley

expect object PlatformSpecificConfig {
    val type: PlatformType
    val defaultPageSize: Int
    val showPagingButtons: Boolean
    val scrollbarsAlwaysVisible: Boolean
}

enum class PlatformType {
    ANDROID, DESKTOP, WASM
}
