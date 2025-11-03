package com.thekeeperofpie.artistalleydatabase.image.crop

import com.eygraber.uri.Uri
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

expect class CropController {
    constructor(params: CropControllerParams, scope: CoroutineScope)

    val cropResults: MutableSharedFlow<CropState.CropResult>

    fun onImageCropDocumentChosen(request: CropState.CropRequest?, cropUri: Uri?)
    fun onConfirmCropInstructions(request: CropState.CropRequest)
    fun onRequestCrop(request: CropState.CropRequest)
    fun onCropFinished(request: CropState.CropRequest?)
}

expect class CropControllerParams

@Inject
class CropControllerFactory(private val params: CropControllerParams) {
    fun create(scope: CoroutineScope) = CropController(params, scope)
}
