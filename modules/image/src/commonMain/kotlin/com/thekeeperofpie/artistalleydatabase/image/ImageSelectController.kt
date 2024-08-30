package com.thekeeperofpie.artistalleydatabase.image

import androidx.compose.runtime.Composable
import com.eygraber.uri.Uri

@Composable
expect fun rememberImageSelectController(
    onAddition: (List<Uri>) -> Unit,
    onSelection: (index: Int, Uri?) -> Unit,
): ImageSelectController

expect class ImageSelectController {
    fun requestNewImages()
    fun requestNewImage(index: Int)
}
