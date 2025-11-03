package com.thekeeperofpie.artistalleydatabase.image.crop

import com.eygraber.uri.Uri
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

actual class CropController actual constructor(
    params: CropControllerParams,
    scope: CoroutineScope,
) {
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

@Inject
actual class CropControllerParams
