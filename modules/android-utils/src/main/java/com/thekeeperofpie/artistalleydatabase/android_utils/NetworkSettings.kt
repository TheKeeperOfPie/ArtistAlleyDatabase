package com.thekeeperofpie.artistalleydatabase.android_utils

interface NetworkSettings {

    var networkLoggingLevel: NetworkLoggingLevel?

    enum class NetworkLoggingLevel {
        NONE, BASIC, HEADERS, BODY
    }
}