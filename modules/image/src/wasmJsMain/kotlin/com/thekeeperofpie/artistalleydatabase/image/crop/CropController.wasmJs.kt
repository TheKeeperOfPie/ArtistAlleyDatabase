package com.thekeeperofpie.artistalleydatabase.image.crop

import com.eygraber.uri.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import me.tatarka.inject.annotations.Inject

@Inject
actual class CropController {

    actual val cropResults = MutableSharedFlow<CropState.CropResult>()

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
