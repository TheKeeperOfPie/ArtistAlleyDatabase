package com.thekeeperofpie.artistalleydatabase.alley

expect object PlatformSpecificConfig {
    val type: PlatformType
    val defaultPageSize: Int
    val showPagingButtons: Boolean
    val scrollbarsAlwaysVisible: Boolean
    val installable: Boolean

    fun requestInstall()
}

enum class PlatformType {
    ANDROID, DESKTOP, WEB
}
