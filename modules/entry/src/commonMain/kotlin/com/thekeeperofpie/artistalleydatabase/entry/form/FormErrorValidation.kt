package com.thekeeperofpie.artistalleydatabase.entry.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import artistalleydatabase.modules.entry.generated.resources.Res
import artistalleydatabase.modules.entry.generated.resources.entry_error_invalid_link
import artistalleydatabase.modules.entry.generated.resources.entry_error_invalid_link_scheme
import artistalleydatabase.modules.entry.generated.resources.entry_error_invalid_number
import artistalleydatabase.modules.entry.generated.resources.entry_error_invalid_uuid
import com.eygraber.uri.Uri
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Stable
@Composable
fun rememberUuidValidator(state: EntryForm2.SingleTextState): State<String?> {
    val errorMessage = stringResource(Res.string.entry_error_invalid_uuid)
    return remember(state) {
        derivedStateOf {
            val text = state.value.text
            when {
                text.isEmpty() -> null
                Uuid.parseOrNull(text.toString()) == null -> errorMessage
                else -> null
            }
        }
    }
}

@Stable
@Composable
fun rememberLongValidator(state: EntryForm2.SingleTextState): State<String?> {
    val errorMessage = stringResource(Res.string.entry_error_invalid_number)
    return remember(state) {
        derivedStateOf {
            val text = state.value.text
            when {
                text.isEmpty() -> null
                text.toString().toLongOrNull() == null -> errorMessage
                else -> null
            }
        }
    }
}

@Stable
@Composable
fun rememberLinkValidator(state: EntryForm2.SingleTextState): State<String?> {
    val genericErrorMessage = stringResource(resource = Res.string.entry_error_invalid_link)
    val schemeErrorMessage = stringResource(Res.string.entry_error_invalid_link_scheme)
    return remember(state) {
        derivedStateOf {
            val text = state.value.text
            if (text.isBlank()) return@derivedStateOf null
            val uri = Uri.parseOrNull(text.toString())
                ?: return@derivedStateOf genericErrorMessage
            if (uri.scheme != "https") {
                schemeErrorMessage
            } else {
                null
            }
        }
    }
}
