package com.thekeeperofpie.artistalleydatabase.utils.kotlin.serialization

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.serialization.ExperimentalSerializationApi
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


object BigDecimalSerializer : KSerializer<BigDecimal?> {

    override val descriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder) = if (decoder.decodeNotNullMark()) {
        try {
            BigDecimal.parseString(decoder.decodeString())
        } catch (e: Exception) {
            null
        }
    } else {
        decoder.decodeNull()
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: BigDecimal?) {
        val serializedValue = value?.toPlainString()
        if (serializedValue == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeNotNullMark()
            encoder.encodeString(serializedValue)
        }
    }
}
