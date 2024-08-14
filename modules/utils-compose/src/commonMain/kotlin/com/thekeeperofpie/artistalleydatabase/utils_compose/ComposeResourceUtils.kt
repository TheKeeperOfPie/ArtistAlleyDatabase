package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource

object ComposeResourceUtils {

    // TODO: Replace this once every module imports the multiplatform library
    @Composable
    fun stringResource(resource: StringResource) =
        org.jetbrains.compose.resources.stringResource(resource)
}
