package com.thekeeperofpie.artistalleydatabase.utils

import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.util.Date

object Converters {

    object DateConverter : JsonAdapter<Date>() {

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

    object StringListConverter {

        @TypeConverter
        fun serializeStringList(value: List<String>?) = Json.encodeToString(value)

        @TypeConverter
        fun deserializeStringList(value: String?) =
            value?.let<String, List<String>>(Json.Default::decodeFromString).orEmpty()
    }

    object IntListConverter {

        @TypeConverter
        fun serializeIntList(value: List<Int>?) = Json.encodeToString(value)

        @TypeConverter
        fun deserializeIntList(value: String?) =
            value?.let<String, List<Int>>(Json.Default::decodeFromString).orEmpty()
    }

    object BigDecimalConverter : JsonAdapter<BigDecimal>() {

        @TypeConverter
        fun serializeBigDecimal(value: BigDecimal?) = value?.toPlainString()

        @TypeConverter
        fun deserializeBigDecimal(value: String?) = value?.let {
            try {
                BigDecimal(it)
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
}