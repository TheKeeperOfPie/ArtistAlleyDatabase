package com.thekeeperofpie.artistalleydatabase.alley.edit.artist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import artistalleydatabase.modules.alley.edit.generated.resources.Res
import artistalleydatabase.modules.alley.edit.generated.resources.alley_edit_artist_error_booth
import com.thekeeperofpie.artistalleydatabase.entry.form.EntryForm2
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberLinkValidator
import com.thekeeperofpie.artistalleydatabase.entry.form.rememberUuidValidator
import org.jetbrains.compose.resources.stringResource

// Not saved since it's purely derived from input fields
@Stable
class ArtistErrorState(
    val idErrorMessage: () -> String?,
    val boothErrorMessage: () -> String?,
    val socialLinksErrorMessage: () -> String?,
    val storeLinksErrorMessage: () -> String?,
    val catalogLinksErrorMessage: () -> String?,
) {
    val hasAnyError by derivedStateOf {
        idErrorMessage() != null ||
                boothErrorMessage() != null ||
                socialLinksErrorMessage() != null ||
                storeLinksErrorMessage() != null ||
                catalogLinksErrorMessage() != null
    }
}

@Stable
@Composable
fun rememberErrorState(state: ArtistFormState): ArtistErrorState {
    val idErrorMessage by rememberUuidValidator(state.editorState.id)
    val boothErrorMessage by rememberBoothValidator(state.info.booth)
    val socialLinksErrorMessage by rememberLinkValidator(state.links.stateSocialLinks)
    val storeLinksErrorMessage by rememberLinkValidator(state.links.stateStoreLinks)
    val catalogLinksErrorMessage by rememberLinkValidator(state.links.stateCatalogLinks)
    return ArtistErrorState(
        idErrorMessage = { idErrorMessage },
        boothErrorMessage = { boothErrorMessage },
        socialLinksErrorMessage = { socialLinksErrorMessage },
        storeLinksErrorMessage = { storeLinksErrorMessage },
        catalogLinksErrorMessage = { catalogLinksErrorMessage },
    )
}

@Stable
@Composable
fun rememberBoothValidator(boothState: EntryForm2.SingleTextState): State<String?> {
    val errorMessage = stringResource(Res.string.alley_edit_artist_error_booth)
    return remember {
        derivedStateOf {
            val booth = boothState.value.text.toString()
            if (booth.isNotBlank() && (
                        booth.length != 3 ||
                                !booth.first().isLetter() ||
                                booth.drop(1).toIntOrNull() == null)
            ) {
                errorMessage
            } else {
                null
            }
        }
    }
}
