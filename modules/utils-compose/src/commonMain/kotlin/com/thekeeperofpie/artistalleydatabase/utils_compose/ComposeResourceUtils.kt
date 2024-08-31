package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource

object ComposeResourceUtils {

    // TODO: Replace this once every module imports the multiplatform library
    @Composable
    fun stringResource(resource: StringResource) =
        org.jetbrains.compose.resources.stringResource(resource)
    @Composable
    fun stringResource(resource: StringResource, vararg formatArgs: Any) =
        org.jetbrains.compose.resources.stringResource(resource, formatArgs)

    @Composable
    fun stringResourceCompat(resource: StringResourceCompat) = resource.text()
}
