package com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TrimmingStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization.TrimmingStringSerializer",
        PrimitiveKind.STRING,
    )

    override fun serialize(encoder: Encoder, value: String?) =
        encoder.encodeString(value?.trim().orEmpty())

    override fun deserialize(decoder: Decoder) = decoder.decodeString().trim()
}
