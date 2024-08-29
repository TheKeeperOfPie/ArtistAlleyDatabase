package com.thekeeperofpie.artistalleydatabase.image.crop

import com.eygraber.uri.Uri

actual class CropController {

    actual fun onImageCropDocumentChosen(
        request: CropState.CropRequest?,
        cropUri: Uri?,
    ) {
    }

    actual fun onConfirmCropInstructions(request: CropState.CropRequest) {
    }

    actual fun onRequestCrop(request: CropState.CropRequest) {
    }

    actual fun onCropFinished(request: CropState.CropRequest?) {
    }
}
