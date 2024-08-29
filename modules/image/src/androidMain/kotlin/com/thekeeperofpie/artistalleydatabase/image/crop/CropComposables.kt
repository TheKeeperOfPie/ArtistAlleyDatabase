package com.thekeeperofpie.artistalleydatabase.image.crop

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.net.toUri
import com.eygraber.uri.toUriOrNull

@Composable
fun CropRequestDialog(cropController: CropController) {
    val cropState = cropController.cropState
    val imageCropDocumentLauncher = if (cropState.imageCropUri == null) {
        rememberLauncherForActivityResult(
            object :
                ActivityResultContract<CropState.CropRequest, Pair<CropState.CropRequest, Uri>?>() {
                private var request: CropState.CropRequest? = null

                @CallSuper
                override fun createIntent(
                    context: Context,
                    input: CropState.CropRequest,
                ): Intent {
                    request = input
                    return Intent(Intent.ACTION_CREATE_DOCUMENT)
                        .setType("image/png")
                        .putExtra(Intent.EXTRA_TITLE, CropUtils.CROP_IMAGE_FILE_NAME)
                        .putExtra(
                            DocumentsContract.EXTRA_INITIAL_URI,
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS
                            ).toUri()
                        )
                }

                override fun parseResult(
                    resultCode: Int,
                    intent: Intent?,
                ): Pair<CropState.CropRequest, Uri>? {
                    val request = request ?: return null
                    val imageUri = intent.takeIf { resultCode == Activity.RESULT_OK }?.data
                        ?: return null
                    return request to imageUri
                }
            }
        ) {
            it ?: return@rememberLauncherForActivityResult
            cropController.onImageCropDocumentChosen(it.first, it.second.toUriOrNull())
        }
    } else null

    val imageCropLauncher = rememberLauncherForActivityResult(
        object : ActivityResultContract<CropState.CropRequest, CropState.CropRequest?>() {
            private var request: CropState.CropRequest? = null

            override fun createIntent(context: Context, input: CropState.CropRequest): Intent {
                request = input
                return CropUtils.cropIntent()
            }

            override fun parseResult(resultCode: Int, intent: Intent?) =
                request.takeIf { resultCode == Activity.RESULT_OK }
        }
    ) { cropController.onCropFinished(it) }

    val cropDocumentRequest = cropState.cropDocumentRequest
    LaunchedEffect(cropDocumentRequest) {
        if (cropDocumentRequest != null) {
            imageCropDocumentLauncher?.launch(cropDocumentRequest)
        }
    }

    val cropReadyRequest = cropState.cropReadyRequest
    LaunchedEffect(cropReadyRequest) {
        if (cropReadyRequest != null) {
            imageCropLauncher.launch(cropReadyRequest)
        }
    }

    val cropInstructionsRequest = cropState.cropInstructionsRequest
    if (cropInstructionsRequest != null) {
        CropUtils.InstructionsDialog(
            onDismiss = { cropState.cropInstructionsRequest = null },
            onConfirm = { cropController.onConfirmCropInstructions(cropInstructionsRequest) },
        )
    }
}
