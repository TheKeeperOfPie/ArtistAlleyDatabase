package com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization

import kotlinx.io.Source
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeSourceToSequence
import kotlinx.serialization.json.jsonArray

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Json.decodeSequenceIgnoreEndOfFile(
    source: Source,
    format: DecodeSequenceMode = DecodeSequenceMode.ARRAY_WRAPPED,
): Sequence<T> {
    val decoded = decodeSourceToSequence<T>(source, format)
    return sequence<T> {
        val iterator = decoded.iterator()
        while (JsonUtils.hasNextIgnoreEndOfFile(iterator)) {
            val next = iterator.next()
            yield(next)
        }
    }
}

fun Json.parseStringList(input: String) = parseToJsonElement(input).jsonArray.map { it.toString() }

object JsonUtils {
    fun hasNextIgnoreEndOfFile(iterator: Iterator<*>) = try {
        iterator.hasNext()
    } catch (throwable: Throwable) {
        // This is a bad hack to get around the fact the array is wrapped in another object
        // and the decoding mechanism can't handle that.
        if (throwable.message?.contains("Expected EOF") == true) {
            false
        } else {
            throw throwable
        }
    }
}
