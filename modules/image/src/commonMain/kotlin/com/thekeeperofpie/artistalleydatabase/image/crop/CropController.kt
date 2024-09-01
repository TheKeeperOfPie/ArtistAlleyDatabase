package com.thekeeperofpie.artistalleydatabase.image.crop

import com.eygraber.uri.Uri
import kotlinx.coroutines.flow.MutableSharedFlow

expect class CropController {
    val cropResults: MutableSharedFlow<CropState.CropResult>

    fun onImageCropDocumentChosen(request: CropState.CropRequest?, cropUri: Uri?)
    fun onConfirmCropInstructions(request: CropState.CropRequest)
    fun onRequestCrop(request: CropState.CropRequest)
    fun onCropFinished(request: CropState.CropRequest?)
}
