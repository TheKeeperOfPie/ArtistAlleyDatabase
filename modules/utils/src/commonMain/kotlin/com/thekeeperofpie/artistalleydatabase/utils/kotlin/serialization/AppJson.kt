package com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization

import kotlinx.serialization.json.Json

open class AppJson {

    open val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }
}
