package com.thekeeperofpie.artistalleydatabase.android_utils

import android.net.Uri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

open class AppJson {

    open val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
        serializersModule = SerializersModule {
            @Suppress("UNCHECKED_CAST")
            contextual(Uri::class, Converters.UriConverter as KSerializer<Uri>)
        }
    }
}