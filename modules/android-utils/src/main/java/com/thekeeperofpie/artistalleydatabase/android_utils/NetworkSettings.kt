package com.thekeeperofpie.artistalleydatabase.android_utils

import kotlinx.coroutines.flow.MutableStateFlow

interface NetworkSettings {

    val networkLoggingLevel: MutableStateFlow<NetworkLoggingLevel>

    enum class NetworkLoggingLevel {
        NONE, BASIC, HEADERS, BODY
    }
}