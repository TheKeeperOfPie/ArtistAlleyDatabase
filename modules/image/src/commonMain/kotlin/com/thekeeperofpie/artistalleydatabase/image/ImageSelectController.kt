package com.thekeeperofpie.artistalleydatabase.image

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImageSelectController(): ImageSelectController

expect class ImageSelectController {
    fun requestNewImages()
    fun requestNewImage(index: Int)
}
