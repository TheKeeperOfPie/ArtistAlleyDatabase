package com.thekeeperofpie.artistalleydatabase.alley.edit.rallies

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import com.thekeeperofpie.artistalleydatabase.alley.edit.artist.rememberBoothValidator
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLinkValidator
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberUuidValidator

// Not saved since it's purely derived from input fields
@Stable
class StampRallyErrorState(
    val idErrorMessage: () -> String?,
    val linksErrorMessage: () -> String?,
) {
    val hasAnyError by derivedStateOf {
        idErrorMessage() != null ||
                linksErrorMessage() != null
    }
}

// TODO: Errors for other fields
@Stable
@Composable
internal fun rememberErrorState(state: StampRallyFormState): StampRallyErrorState {
    val idErrorMessage by rememberUuidValidator(state.editorState.id)
    val linksErrorMessage by rememberLinkValidator(state.stateLinks)
    return StampRallyErrorState(
        idErrorMessage = { idErrorMessage },
        linksErrorMessage = { linksErrorMessage },
    )
}
