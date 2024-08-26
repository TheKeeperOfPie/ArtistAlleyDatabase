package com.thekeeperofpie.artistalleydatabase.android_utils

import androidx.room.TypeConverter
import com.ionspin.kotlin.bignum.decimal.BigDecimal
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
import java.util.Date

object Converters {

    object DateConverter : KSerializer<Date?> {

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
    }

    object LocalDateConverter {

        @TypeConverter
        fun serializeDate(value: LocalDate?) = Json.encodeToString(value)

        @TypeConverter
        fun deserializeDate(value: String) = Json.decodeFromString<LocalDate>(value)
    }

    object InstantConverter {

        @TypeConverter
        fun serializeInstant(value: Instant?) = value?.toEpochMilliseconds()

        @TypeConverter
        fun deserializeInstant(value: Long) = Instant.fromEpochMilliseconds(value)
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

    object BigDecimalConverter : KSerializer<BigDecimal?> {

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
    }
}
