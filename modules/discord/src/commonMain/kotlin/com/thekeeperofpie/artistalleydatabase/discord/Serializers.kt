package com.thekeeperofpie.artistalleydatabase.discord

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.enums.EnumEntries

@OptIn(ExperimentalSerializationApi::class)
abstract class IntEnumSerializer<T : Enum<T>>(
    private val entries: EnumEntries<T>,
    serialName: String,
    private val value: (T) -> Int,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName, PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): T {
        val value = decoder.decodeInt()
        return entries.first { value(it) == value }
    }

    override fun serialize(
        encoder: Encoder,
        value: T,
    ) {
        encoder.encodeInt(value(value))
    }
}

@OptIn(ExperimentalSerializationApi::class)
abstract class NullableStringEnumSerializer<T : Enum<T>>(
    private val entries: EnumEntries<T>,
    serialName: String,
    private val value: (T) -> String,
) : KSerializer<T?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): T? {
        val value = decoder.decodeString()
        return entries.find { value(it) == value }
    }

    override fun serialize(
        encoder: Encoder,
        value: T?,
    ) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value(value))
        }
    }
}
