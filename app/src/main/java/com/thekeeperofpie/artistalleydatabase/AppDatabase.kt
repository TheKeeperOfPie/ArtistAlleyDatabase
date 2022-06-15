@file:Suppress("OPT_IN_USAGE")

package com.thekeeperofpie.artistalleydatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.thekeeperofpie.artistalleydatabase.Converters.DateConverter.deserializeDate
import com.thekeeperofpie.artistalleydatabase.Converters.DateConverter.serializeDate
import com.thekeeperofpie.artistalleydatabase.art.ArtEntry
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryDao
import com.thekeeperofpie.artistalleydatabase.art.ArtEntryFts
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull.serializer
import kotlinx.serialization.serializer
import java.math.BigDecimal
import java.util.Date

@Database(entities = [ArtEntry::class, ArtEntryFts::class], exportSchema = false, version = 1)
@TypeConverters(value = [
    Converters.DateConverter::class,
    Converters.StringListConverter::class,
    Converters.BigDecimalConverter::class
])
abstract class AppDatabase : RoomDatabase() {
    abstract fun artEntryDao(): ArtEntryDao
}

object Converters {

    val KSERIALIZERS = mapOf(
        Date::class to DateConverter,
        BigDecimal::class to BigDecimalConverter,
    )

    object DateConverter : KSerializer<Date?> {

        override val descriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: Date?) {
            val encoded = serializeDate(value)
            if (encoded == null) {
                encoder.encodeNull()
            } else {
                encoder.encodeLong(encoded)
            }
        }

        @OptIn(ExperimentalSerializationApi::class)
        override fun deserialize(decoder: Decoder): Date? {
            return if (decoder.decodeNotNullMark()) {
                deserializeDate(decoder.decodeLong())
            } else {
                decoder.decodeNull()
            }
        }

        @TypeConverter
        fun serializeDate(value: Date?) = value?.time

        @TypeConverter
        fun deserializeDate(value: Long?) = value?.let { Date(it) }
    }

    object StringListConverter {

        @TypeConverter
        fun serializeStringList(value: List<String>?) = Json.Default.encodeToString(value)

        @TypeConverter
        fun deserializeStringList(value: String?) =
            value?.let<String, List<String>>(Json.Default::decodeFromString).orEmpty()
    }

    object BigDecimalConverter : KSerializer<BigDecimal?> {

        override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: BigDecimal?) {
            val encoded = serializeBigDecimal(value)
            if (encoded == null) {
                encoder.encodeNull()
            } else {
                encoder.encodeString(encoded)
            }
        }

        @OptIn(ExperimentalSerializationApi::class)
        override fun deserialize(decoder: Decoder): BigDecimal? {
            return if (decoder.decodeNotNullMark()) {
                deserializeBigDecimal(decoder.decodeString())
            } else {
                decoder.decodeNull()
            }
        }

        @TypeConverter
        fun serializeBigDecimal(value: BigDecimal?) = value?.toPlainString()

        @TypeConverter
        fun deserializeBigDecimal(value: String?) = value?.let(::BigDecimal)
    }
}