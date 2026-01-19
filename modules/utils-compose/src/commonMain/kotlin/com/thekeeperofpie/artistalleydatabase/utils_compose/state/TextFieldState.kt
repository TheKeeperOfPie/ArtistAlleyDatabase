package com.thekeeperofpie.artistalleydatabase.utils_compose.state

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.saveable.SaverScope

/** Bug in TextFieldState.Saver, doesn't work on web, just restore the text for now */
private object FixedTextFieldStateSaver : ComposeSaver<TextFieldState, Any> {
    override fun SaverScope.save(value: TextFieldState) = value.text.toString()
    override fun restore(value: Any) = TextFieldState(initialText = value as String)
}

@Suppress("UnusedReceiverParameter")
val TextFieldState.Saver.Fixed: ComposeSaver<TextFieldState, Any> get() = FixedTextFieldStateSaver
