package com.thekeeperofpie.artistalleydatabase.image

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

@Composable
actual fun rememberImageHandler(): ImageHandler = ImageHandler()

actual class ImageHandler {
    actual fun openImage(uri: Uri) {
        // TODO
    }
}
