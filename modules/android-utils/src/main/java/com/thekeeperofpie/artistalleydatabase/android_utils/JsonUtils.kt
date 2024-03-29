package com.thekeeperofpie.artistalleydatabase.android_utils

import android.util.JsonReader
import android.util.JsonToken
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType
import com.squareup.moshi.JsonReader as MoshiReader

object JsonUtils {

    fun readStringList(input: String): List<String> = JsonReader(input.reader()).use { reader ->
        reader.isLenient = true
        if (reader.peek() != JsonToken.BEGIN_ARRAY) {
            return emptyList()
        }

        reader.beginArray()

        val result = mutableListOf<String>()
        while (reader.peek() != JsonToken.END_ARRAY) {
            result += reader.nextString()
        }
        reader.endArray()
        return@use result
    }
}

fun MoshiReader.readNullableString() = when (peek()) {
    MoshiReader.Token.NULL -> nextNull<String>()
    MoshiReader.Token.STRING -> {
        val value = nextString()
        if (value == "null") null else value
    }
    else -> {
        skipValue()
        null
    }
}

fun MoshiReader.readNullableLong() = when (peek()) {
    MoshiReader.Token.NULL -> nextNull<Long>()
    MoshiReader.Token.STRING -> nextString().toLongOrNull()
    MoshiReader.Token.NUMBER -> nextLong()
    else -> {
        skipValue()
        null
    }
}

fun Json.encodeToString(kType: KType, value: Any) =
    encodeToString(serializersModule.serializer(kType), value)