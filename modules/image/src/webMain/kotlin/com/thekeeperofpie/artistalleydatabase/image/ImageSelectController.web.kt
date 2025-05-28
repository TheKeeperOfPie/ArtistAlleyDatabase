package com.thekeeperofpie.artistalleydatabase.image

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

@Composable
actual fun rememberImageSelectController(
    onAddition: (List<Uri>) -> Unit,
    onSelection: (index: Int, Uri?) -> Unit,
): ImageSelectController {
    return ImageSelectController()
}

actual class ImageSelectController {
    actual fun requestNewImages() {
        // TODO
    }

    actual fun requestNewImage(index: Int) {
        // TODO
    }
}
