package com.thekeeperofpie.artistalleydatabase.android_utils

import androidx.room.TypeConverter
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import java.util.Date

object Converters {

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

        fun serializeDate(value: Date?) = value?.time

        fun deserializeDate(value: Long?) = value?.let { Date(it) }

        override fun fromJson(reader: JsonReader) =
            deserializeDate(reader.readNullableLong())

        override fun toJson(writer: JsonWriter, value: Date?) {
            writer.value(serializeDate(value))
        }
    }

    object LocalDateConverter : JsonAdapter<LocalDate>() {

        @TypeConverter
        fun serializeDate(value: LocalDate?) = Json.encodeToString(value)

        @TypeConverter
        fun deserializeDate(value: String) = Json.decodeFromString<LocalDate>(value)

        override fun fromJson(reader: JsonReader) =
            reader.readNullableString()?.let(::deserializeDate)

        override fun toJson(writer: JsonWriter, value: LocalDate?) {
            writer.value(serializeDate(value))
        }
    }

    object InstantConverter : JsonAdapter<Instant>() {

        @TypeConverter
        fun serializeInstant(value: Instant?) = value?.toEpochMilliseconds()

        @TypeConverter
        fun deserializeInstant(value: Long) = Instant.fromEpochMilliseconds(value)

        override fun fromJson(reader: JsonReader) =
            reader.readNullableLong()?.let(::deserializeInstant)

        override fun toJson(writer: JsonWriter, value: Instant?) {
            writer.value(serializeInstant(value))
        }
    }

    object StringListConverter {

        @TypeConverter
        fun serializeStringList(value: List<String>?) = Json.encodeToString(value)

        @TypeConverter
        fun deserializeStringList(value: String?) =
            value?.let<String, List<String>>(Json.Default::decodeFromString).orEmpty()
    }

    object StringMapConverter {

        @TypeConverter
        fun serializeStringMap(value: Map<String, String>?) = Json.encodeToString(value)

        @TypeConverter
        fun deserializeStringMap(value: String?) =
            value?.let<String, Map<String, String>>(Json.Default::decodeFromString).orEmpty()
    }

    object IntListConverter {

        @TypeConverter
        fun serializeIntList(value: List<Int>?) = Json.encodeToString(value)

        @TypeConverter
        fun deserializeIntList(value: String?) =
            value?.let<String, List<Int>>(Json.Default::decodeFromString).orEmpty()
    }

    object BigDecimalConverter : JsonAdapter<BigDecimal>(), KSerializer<BigDecimal?> {

        override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

        @OptIn(ExperimentalSerializationApi::class)
        override fun deserialize(decoder: Decoder) = if (decoder.decodeNotNullMark()) {
            deserializeBigDecimal(decoder.decodeString())
        } else {
            decoder.decodeNull()
        }

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: BigDecimal?) {
            val serializedValue = serializeBigDecimal(value)
            if (serializedValue == null) {
                encoder.encodeNull()
            } else {
                encoder.encodeNotNullMark()
                encoder.encodeString(serializedValue)
            }
        }

        @TypeConverter
        fun serializeBigDecimal(value: BigDecimal?) = value?.toPlainString()

        @TypeConverter
        fun deserializeBigDecimal(value: String?) = value?.let {
            try {
                BigDecimal.parseString(it)
            } catch (e: Exception) {
                null
            }
        }

        override fun fromJson(reader: JsonReader): BigDecimal? {
            return deserializeBigDecimal(reader.readNullableString())
        }

        override fun toJson(writer: JsonWriter, value: BigDecimal?) {
            writer.value(serializeBigDecimal(value))
        }
    }

    object JsonElementConverter : JsonAdapter<JsonElement>() {

        override fun fromJson(reader: JsonReader) =
            throw UnsupportedOperationException("Cannot read back JsonElement")

        override fun toJson(writer: JsonWriter, value: JsonElement?) {
            if (value == null) {
                writer.nullValue()
                return
            }

            writeElement(writer, value)
        }

        private fun writeElement(writer: JsonWriter, value: JsonElement) {
            when (value) {
                is JsonPrimitive -> {
                    val intOrNull = value.intOrNull
                    if (intOrNull != null) {
                        writer.value(value.int)
                        return
                    }

                    val longOrNull = value.longOrNull
                    if (longOrNull != null) {
                        writer.value(longOrNull)
                        return
                    }

                    val doubleOrNull = value.doubleOrNull
                    if (doubleOrNull != null) {
                        writer.value(doubleOrNull)
                        return
                    }

                    val floatOrNull = value.floatOrNull
                    if (floatOrNull != null) {
                        writer.value(floatOrNull)
                        return
                    }

                    val booleanOrNull = value.booleanOrNull
                    if (booleanOrNull != null) {
                        writer.value(booleanOrNull)
                        return
                    }

                    writer.value(value.contentOrNull)
                }
                JsonNull -> writer.nullValue()
                is JsonObject -> {
                    writer.beginObject()
                    value.entries.forEach { (name, element) ->
                        writer.name(name)
                        writeElement(writer, element)
                    }
                    writer.endObject()
                }
                is JsonArray -> {
                    writer.beginArray()
                    value.forEach {
                        writeElement(writer, it)
                    }
                    writer.endArray()
                }
            }
        }
    }
}
