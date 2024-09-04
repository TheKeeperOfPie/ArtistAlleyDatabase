package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource

// TODO: Replace this once every module imports the multiplatform library
object ComposeResourceUtils {

    @Composable
    fun stringResource(resource: Int) = stringResourceCompat(StringResourceId(resource))

    @Composable
    fun stringResource(resource: StringResource) =
        org.jetbrains.compose.resources.stringResource(resource)
    @Composable
    fun stringResource(resource: StringResource, vararg formatArgs: Any) =
        org.jetbrains.compose.resources.stringResource(resource, formatArgs)

    @Composable
    fun stringResource(resource: StringResourceCompat) = resource.text()

    @Composable
    fun stringResourceCompat(resource: StringResourceCompat) = resource.text()
}
