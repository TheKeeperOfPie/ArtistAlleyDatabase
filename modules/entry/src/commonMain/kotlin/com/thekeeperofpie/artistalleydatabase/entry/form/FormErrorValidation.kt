package com.thekeeperofpie.artistalleydatabase.entry.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import artistalleydatabase.modules.entry.generated.resources.Res
import artistalleydatabase.modules.entry.generated.resources.entry_error_invalid_link
import artistalleydatabase.modules.entry.generated.resources.entry_error_invalid_link_scheme
import artistalleydatabase.modules.entry.generated.resources.entry_error_invalid_number
import artistalleydatabase.modules.entry.generated.resources.entry_error_invalid_uuid
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.utils_compose.state.ComposeSaver
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Serializable
sealed interface FormErrorValidation {

    @Stable
    class Clearable(text: String?) : FormErrorValidation {
        var text by mutableStateOf(text)

        object Saver : ComposeSaver<Clearable, String> {
            override fun SaverScope.save(value: Clearable) = value.text.orEmpty()
            override fun restore(value: String) = Clearable(value.ifBlank { null })
        }
    }

    fun interface Derived : FormErrorValidation {
        operator fun invoke(text: CharSequence): String?
    }
}

@Composable
fun rememberClearableValidator(): FormErrorValidation.Clearable =
    rememberSaveable(FormErrorValidation.Clearable.Saver) {
        FormErrorValidation.Clearable(
            null
        )
    }

@Stable
@Composable
fun rememberUuidValidator(): FormErrorValidation.Derived =
    UuidValidator(stringResource(Res.string.entry_error_invalid_uuid))

@Stable
@Composable
fun rememberLongValidator(): FormErrorValidation.Derived =
    LongValidator(stringResource(Res.string.entry_error_invalid_number))

@Stable
@Composable
fun rememberLinkValidator(): FormErrorValidation.Derived =
    LinkValidator(
        stringResource(resource = Res.string.entry_error_invalid_link),
        stringResource(Res.string.entry_error_invalid_link_scheme)
    )

private data class UuidValidator(private val errorMessage: String) : FormErrorValidation.Derived {
    override fun invoke(text: CharSequence) =
        when {
            text.isEmpty() -> null
            Uuid.parseOrNull(text.toString()) == null -> errorMessage
            else -> null
        }
}

private data class LongValidator(private val errorMessage: String) : FormErrorValidation.Derived {
    override fun invoke(text: CharSequence) =
        when {
            text.isEmpty() -> null
            text.toString().toLongOrNull() == null -> errorMessage
            else -> null
        }
}

private data class LinkValidator(
    private val genericErrorMessage: String,
    private val schemeErrorMessage: String,
) : FormErrorValidation.Derived {
    override fun invoke(text: CharSequence): String? {
        if (text.isBlank()) return null
        val uri = Uri.parseOrNull(text.toString()) ?: return genericErrorMessage
        if (uri.scheme != "https") {
            return schemeErrorMessage
        }
        return null
    }
}
