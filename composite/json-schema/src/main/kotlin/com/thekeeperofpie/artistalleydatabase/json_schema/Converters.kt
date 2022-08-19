package com.thekeeperofpie.artistalleydatabase.json_schema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object Converters {

    object RegexConverter : KSerializer<Regex?> {
        override val descriptor =
            PrimitiveSerialDescriptor(Regex::class.simpleName!!, PrimitiveKind.STRING)

        @OptIn(ExperimentalSerializationApi::class)
        override fun deserialize(decoder: Decoder) =
            if (decoder.decodeNotNullMark()) {
                try {
                    decoder.decodeString().toRegex()
                } catch (e: Exception) {
                    null
                }
            } else {
                decoder.decodeNull()
                null
            }

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: Regex?) =
            if (value == null) {
                encoder.encodeNull()
            } else {
                encoder.encodeNotNullMark()
                encoder.encodeString(value.toString())
            }

    }
}