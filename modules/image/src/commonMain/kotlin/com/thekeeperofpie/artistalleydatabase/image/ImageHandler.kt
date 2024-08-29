package com.thekeeperofpie.artistalleydatabase.image

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

@Composable
expect fun rememberImageHandler(): ImageHandler

expect class ImageHandler {
    fun openImage(uri: Uri)
}
