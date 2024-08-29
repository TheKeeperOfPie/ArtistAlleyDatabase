package com.thekeeperofpie.artistalleydatabase.image

import com.eygraber.uri.Uri

expect class ImageHandler {
    fun openImage(uri: Uri)
}
