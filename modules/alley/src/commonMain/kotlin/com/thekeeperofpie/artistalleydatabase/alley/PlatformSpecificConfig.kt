package com.thekeeperofpie.artistalleydatabase.alley

expect object PlatformSpecificConfig {
    val type: PlatformType
    val defaultPageSize: Int
    val showPagingButtons: Boolean
}

enum class PlatformType {
    ANDROID, DESKTOP, WASM
}
