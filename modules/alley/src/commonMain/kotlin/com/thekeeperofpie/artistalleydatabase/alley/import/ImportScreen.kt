package com.thekeeperofpie.artistalleydatabase.alley.import

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import_prompt_cancel
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import_prompt_close
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import_prompt_confirm
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import_prompt_title
import artistalleydatabase.modules.alley.generated.resources.alley_settings_import_success
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import org.jetbrains.compose.resources.stringResource

internal object ImportScreen {

    @Composable
    operator fun invoke(
        state: () -> LoadingResult<*>,
        importData: String,
        onDismiss: () -> Unit,
        onConfirmImport: () -> Unit,
    ) {
        val state = state()
        val success = state.success
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                val error = state.error
                Text(
                    when {
                        success -> stringResource(Res.string.alley_settings_import_success)
                        error != null -> error.messageText()
                        else -> stringResource(Res.string.alley_settings_import_prompt_title)
                    }
                )
            },
            text = if (state.success) null else {
                { Text(state.error?.throwable?.message ?: importData) }
            },
            confirmButton = {
                val loading = state.loading
                Button(enabled = !loading, onClick = {
                    if (state.success) {
                        onDismiss()
                    } else if (!loading) {
                        onConfirmImport()
                    }
                }) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            stringResource(
                                if (state.success) {
                                    Res.string.alley_settings_import_prompt_close
                                } else {
                                    Res.string.alley_settings_import_prompt_confirm
                                }
                            )
                        )
                        if (loading) {
                            CircularProgressIndicator()
                        }
                    }
                }
            },
            dismissButton = if (success) null else {
                {
                    Button(onClick = onDismiss) {
                        Text(stringResource(Res.string.alley_settings_import_prompt_cancel))
                    }
                }
            },
        )
    }
}
