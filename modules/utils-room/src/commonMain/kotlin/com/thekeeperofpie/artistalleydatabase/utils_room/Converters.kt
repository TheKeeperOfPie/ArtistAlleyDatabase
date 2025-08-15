package com.thekeeperofpie.artistalleydatabase.utils_room

import androidx.room.TypeConverter
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.time.Instant

object Converters {

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

    object BigDecimalConverter {

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
