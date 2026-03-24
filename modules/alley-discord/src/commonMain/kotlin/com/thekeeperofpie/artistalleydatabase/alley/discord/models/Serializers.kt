package com.thekeeperofpie.artistalleydatabase.alley.discord.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.enums.EnumEntries

@OptIn(ExperimentalSerializationApi::class)
internal abstract class IntEnumSerializer<T : Enum<T>>(
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
internal abstract class NullableIntEnumSerializer<T : Enum<T>>(
    private val entries: EnumEntries<T>,
    serialName: String,
    private val value: (T) -> Int,
) : KSerializer<T?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName, PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): T? {
        val value = decoder.decodeInt()
        return entries.find { value(it) == value }
    }

    override fun serialize(
        encoder: Encoder,
        value: T?,
    ) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeInt(value(value))
        }
    }
}
