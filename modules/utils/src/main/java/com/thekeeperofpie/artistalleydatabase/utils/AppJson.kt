package com.thekeeperofpie.artistalleydatabase.utils

import kotlinx.serialization.json.Json

open class AppJson {

    open val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }
}