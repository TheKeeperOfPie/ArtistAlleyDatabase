package com.thekeeperofpie.artistalleydatabase.utils_compose.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.saveable.SaverScope
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Bug in TextFieldState.Saver, doesn't work on web, just restore the text for now */
private object FixedTextFieldStateSaver : ComposeSaver<TextFieldState, Any> {
    override fun SaverScope.save(value: TextFieldState) = value.text.toString()
    override fun restore(value: Any) = TextFieldState(initialText = value as String)
}

@Suppress("UnusedReceiverParameter")
val TextFieldState.Saver.Fixed: ComposeSaver<TextFieldState, Any> get() = FixedTextFieldStateSaver

object TextFieldStateSerializer  : KSerializer<TextFieldState> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("TextFieldState", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TextFieldState) {
        encoder.encodeString(value.text.toString())
    }

    override fun deserialize(decoder: Decoder) = TextFieldState(decoder.decodeString())
}
