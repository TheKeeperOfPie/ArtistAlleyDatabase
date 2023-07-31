package com.thekeeperofpie.artistalleydatabase.network_utils

import kotlinx.coroutines.flow.MutableStateFlow

interface NetworkSettings {

    val networkLoggingLevel: MutableStateFlow<NetworkLoggingLevel>
    val enableNetworkCaching: MutableStateFlow<Boolean>

    enum class NetworkLoggingLevel {
        NONE, BASIC, HEADERS, BODY
    }
}
