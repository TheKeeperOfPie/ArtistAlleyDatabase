package com.thekeeperofpie.artistalleydatabase.utils_compose.text

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun ForceEnabledTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    label: @Composable (TextFieldLabelScope.() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    inputTransformation: InputTransformation? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = rememberTextFieldColors()
    TextField(
        state = state,
        colors = colors,
        enabled = !readOnly,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        inputTransformation = inputTransformation,
        keyboardOptions = keyboardOptions,
        modifier = modifier,
    )
}

@Composable
private fun rememberTextFieldColors(): TextFieldColors {
    // Force colors to show as normal even when TextField is not enabled to disable
    // focus and label move animation while allowing the text to be easily read
    val defaultColors = TextFieldDefaults.colors()
    return remember(defaultColors) {
        defaultColors.copy(
            disabledTextColor = defaultColors.unfocusedTextColor,
            disabledContainerColor = defaultColors.unfocusedContainerColor,
            disabledIndicatorColor = defaultColors.unfocusedIndicatorColor,
            disabledLabelColor = defaultColors.unfocusedLabelColor,
            disabledPlaceholderColor = defaultColors.unfocusedPlaceholderColor,
        )
    }
}
