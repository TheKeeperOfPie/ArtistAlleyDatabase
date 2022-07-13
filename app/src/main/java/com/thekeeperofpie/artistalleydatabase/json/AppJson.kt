package com.thekeeperofpie.artistalleydatabase.json

import kotlinx.serialization.json.Json

class AppJson {

    val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
    }
}