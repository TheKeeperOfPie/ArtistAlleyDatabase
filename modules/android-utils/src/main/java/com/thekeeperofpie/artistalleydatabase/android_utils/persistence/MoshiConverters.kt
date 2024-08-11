package com.thekeeperofpie.artistalleydatabase.android_utils.persistence

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.thekeeperofpie.artistalleydatabase.android_utils.readNullableLong
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Date

object MoshiConverters {

    object DateConverter : JsonAdapter<Date>(), KSerializer<Date?> {

        override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

        @OptIn(ExperimentalSerializationApi::class)
        override fun deserialize(decoder: Decoder) = if (decoder.decodeNotNullMark()) {
            deserializeDate(decoder.decodeLong())
        } else {
            decoder.decodeNull()
        }

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: Date?) {
            val serializedValue = serializeDate(value)
            if (serializedValue == null) {
                encoder.encodeNull()
            } else {
                encoder.encodeNotNullMark()
                encoder.encodeLong(serializedValue)
            }
        }

        @TypeConverter
        fun serializeDate(value: Date?) = value?.time

        @TypeConverter
        fun deserializeDate(value: Long?) = value?.let { Date(it) }

        override fun fromJson(reader: JsonReader) =
            deserializeDate(reader.readNullableLong())

        override fun toJson(writer: JsonWriter, value: Date?) {
            writer.value(serializeDate(value))
        }
    }
}
