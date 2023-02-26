package com.thekeeperofpie.artistalleydatabase.android_utils

import android.net.Uri
import androidx.room.TypeConverter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
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
import kotlinx.serialization.serializer
import java.math.BigDecimal
import java.util.Date
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object Converters {

    class PropertiesSerializer<T : Any>(
        private val kClass: KClass<T>,
        private val appJson: AppJson,
    ) : KSerializer<T> {
        interface WritableDelegate {
            fun setValue(value: Any?)
        }

        override val descriptor = buildClassSerialDescriptor(kClass.simpleName!!) {
            nonTransientProperties()
                .forEach {
                    element(
                        it.name,
                        appJson.json.serializersModule.serializer(it.returnType).descriptor
                    )
                }
        }

        fun nonTransientProperties() = kClass.memberProperties
            .sortedBy { it.name }
            .filter { it.findAnnotations(Transient::class).isEmpty() }
            .onEach { it.isAccessible = true }

        override fun deserialize(decoder: Decoder): T {
            val data = kClass.createInstance()
            decoder.decodeStructure(descriptor) {
                val properties = nonTransientProperties()
                var elementIndex = decodeElementIndex(descriptor)
                while (elementIndex != CompositeDecoder.DECODE_DONE) {
                    val property = properties[elementIndex]
                    val value = decodeSerializableElement(
                        descriptor,
                        elementIndex,
                        decoder.serializersModule.serializer(property.returnType),
                    )
                    (property.getDelegate(data) as WritableDelegate).setValue(value)

                    elementIndex = decodeElementIndex(descriptor)
                }
            }
            return data
        }

        override fun serialize(encoder: Encoder, value: T) {
            encoder.encodeStructure(descriptor) {
                nonTransientProperties()
                    .forEachIndexed { index, property ->
                        encodeSerializableElement(
                            descriptor,
                            index,
                            encoder.serializersModule.serializer(property.returnType),
                            property.get(value)
                        )
                    }
            }
        }
    }

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

    object UriConverter : KSerializer<Uri?> {

        override val descriptor = PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

        @OptIn(ExperimentalSerializationApi::class)
        override fun deserialize(decoder: Decoder) = if (decoder.decodeNotNullMark()) {
            deserializeUri(decoder.decodeString())
        } else {
            decoder.decodeNull()
        }

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: Uri?) {
            val serializedValue = serializeUri(value)
            if (serializedValue == null) {
                encoder.encodeNull()
            } else {
                encoder.encodeNotNullMark()
                encoder.encodeString(serializedValue)
            }
        }

        @TypeConverter
        fun serializeUri(value: Uri?) = value?.toString()

        @TypeConverter
        fun deserializeUri(value: String?) = value?.let {
            try {
                Uri.parse(it)
            } catch (e: Exception) {
                null
            }
        }
    }
}