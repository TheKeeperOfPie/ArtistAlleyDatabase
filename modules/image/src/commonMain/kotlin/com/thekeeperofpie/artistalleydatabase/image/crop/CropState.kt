package com.thekeeperofpie.artistalleydatabase.image.crop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eygraber.uri.Uri
import org.jetbrains.compose.resources.StringResource

interface CropState {
    val imageCropUri: Uri?
    val cropInstructionsRequest: CropRequest?
    val cropDocumentRequest: CropRequest?
    val cropReadyRequest: CropRequest?
    val error: Pair<StringResource, Throwable?>?

    data class CropRequest(
        val imageFolderName: String,
        val id: String,
        val uri: Uri,
    )

    data class CropResult(
        val request: CropRequest,
        val newImageUri: Uri,
        val croppedWidth: Int?,
        val croppedHeight: Int?,
    )
}

class CropStateImpl : CropState {
    override var imageCropUri by mutableStateOf<Uri?>(null)
    override var cropInstructionsRequest by mutableStateOf<CropState.CropRequest?>(null)
    override var cropDocumentRequest by mutableStateOf<CropState.CropRequest?>(null)
    override var cropReadyRequest by mutableStateOf<CropState.CropRequest?>(null)
    override var error by mutableStateOf<Pair<StringResource, Throwable?>?>(null)
}
