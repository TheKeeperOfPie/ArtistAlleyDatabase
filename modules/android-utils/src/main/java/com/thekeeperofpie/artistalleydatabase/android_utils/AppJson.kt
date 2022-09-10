package com.thekeeperofpie.artistalleydatabase.android_utils

import kotlinx.serialization.json.Json

open class AppJson {

    open val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }
}