package com.thekeeperofpie.artistalleydatabase

import android.util.JsonReader
import android.util.JsonToken

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