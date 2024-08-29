package com.thekeeperofpie.artistalleydatabase.image

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImageSelectController(): ImageSelectController {
    return ImageSelectController()
}

actual class ImageSelectController {
    actual fun requestNewImages() {
        TODO()
    }

    actual fun requestNewImage(index: Int) {
        TODO()
    }
}
