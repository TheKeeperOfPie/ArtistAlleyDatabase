package com.thekeeperofpie.artistalleydatabase.utils_compose.state

import androidx.compose.runtime.snapshots.SnapshotStateSet
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class SnapshotStateSetSerializer<T>(elementSerializer: KSerializer<T>) :
    KSerializer<SnapshotStateSet<T>> {

    private val base = SetSerializer(elementSerializer)

    override val descriptor: SerialDescriptor =
        SerialDescriptor("androidx.compose.runtime.SnapshotStateSet", base.descriptor)

    override fun serialize(encoder: Encoder, value: SnapshotStateSet<T>) {
        encoder.encodeSerializableValue(base, value)
    }

    override fun deserialize(decoder: Decoder): SnapshotStateSet<T> {
        val deserialized = decoder.decodeSerializableValue(base)
        return SnapshotStateSet<T>().apply { addAll(deserialized.toSet()) }
    }
}
