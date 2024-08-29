package com.thekeeperofpie.artistalleydatabase.image.crop

import com.eygraber.uri.Uri

expect class CropController {

    fun onImageCropDocumentChosen(request: CropState.CropRequest?, cropUri: Uri?)
    fun onConfirmCropInstructions(request: CropState.CropRequest)
    fun onRequestCrop(request: CropState.CropRequest)
    fun onCropFinished(request: CropState.CropRequest?)
}
