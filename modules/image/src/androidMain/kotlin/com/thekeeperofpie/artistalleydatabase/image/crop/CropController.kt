package com.thekeeperofpie.artistalleydatabase.image.crop

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import artistalleydatabase.modules.utils_compose.generated.resources.error_fail_to_crop_image
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.toUri
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.io.asOutputStream
import kotlinx.io.files.SystemFileSystem

actual class CropController(
    private val scope: CoroutineScope,
    private val application: Application,
    private val appFileSystem: AppFileSystem,
    private val settings: CropSettings,
) {
    companion object {
        private const val TAG = "CropController"
    }

    val cropState = CropStateImpl()

    actual val cropResults = MutableSharedFlow<CropState.CropResult>()

    /** Shared utility to easily signal URI invalidation by appending a query param of this value */
    private var invalidateIteration = 0

    init {
        scope.launch(Dispatchers.IO) {
            val serializedUri = settings.cropImageUri.value ?: return@launch
            try {
                val uri = Uri.parse(serializedUri)
                appFileSystem.openUriSink(uri)?.close()?.run {
                    launch(Dispatchers.Main) {
                        cropState.imageCropUri = uri
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading crop URI: $serializedUri")
            }
        }
    }

    actual fun onImageCropDocumentChosen(request: CropState.CropRequest?, cropUri: Uri?) {
        request ?: return
        cropUri ?: return
        scope.launch(Dispatchers.IO) {
            try {
                application.contentResolver.takePersistableUriPermission(
                    cropUri.toAndroidUri(),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error persisting URI grant $cropUri")
                launch(Dispatchers.Main) {
                    cropState.error = UtilsStrings.error_fail_to_crop_image to e
                }
                return@launch
            }

            settings.cropImageUri.value = cropUri.toString()
            appFileSystem.openUriSource(request.uri)
                ?.use { input ->
                    appFileSystem.openUriSink(cropUri)?.use { output ->
                        input.transferTo(output)
                    }
                }?.run {
                    launch(Dispatchers.Main) {
                        cropState.imageCropUri = cropUri
                        cropState.cropReadyRequest = request
                    }
                }
        }
    }

    actual fun onConfirmCropInstructions(request: CropState.CropRequest) {
        cropState.cropDocumentRequest = request
    }

    actual fun onRequestCrop(request: CropState.CropRequest) {
        if (cropState.imageCropUri == null) {
            cropState.cropInstructionsRequest = request
        } else {
            onImageRequestCrop(request)
        }
    }

    private fun onImageRequestCrop(request: CropState.CropRequest) {
        val imageCropUri = cropState.imageCropUri ?: return
        scope.launch(Dispatchers.IO) {
            appFileSystem.openUriSource(request.uri)
                ?.use { input ->
                    appFileSystem.openUriSink(imageCropUri)?.use { output ->
                        input.transferTo(output)
                    }
                }

            application.grantUriPermission(
                CropUtils.PHOTOS_PACKAGE_NAME,
                imageCropUri.toAndroidUri(),
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            launch(Dispatchers.Main) {
                cropState.cropReadyRequest = request
            }
        }
    }

    actual fun onCropFinished(request: CropState.CropRequest?) {
        cropState.cropReadyRequest = null
        request ?: return
        val imageCropUri = cropState.imageCropUri ?: return
        scope.launch(Dispatchers.IO) {
            val outputPath =
                CropUtils.getCropTempPath(appFileSystem, request.imageFolderName, request.id)
            appFileSystem.openUriSource(imageCropUri)?.use { input ->
                SystemFileSystem.sink(outputPath).use { output ->
                    input.transferTo(output)
                }
            }

            // Clear the temporary crop file contents
            appFileSystem.openUriSink(imageCropUri, "wt")?.use {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                    .compress(Bitmap.CompressFormat.PNG, 100, it.asOutputStream())
            }

            val (width, height) = appFileSystem.getImageWidthHeight(outputPath.toUri())
            val uri = outputPath.toUri()
                .buildUpon()
                .appendQueryParameter(
                    "invalidateIteration",
                    invalidateIteration++.toString()
                )
                .build()

            launch(Dispatchers.Main) {
                cropState.cropReadyRequest = null
                cropResults.emit(
                    CropState.CropResult(
                        request = request,
                        newImageUri = uri,
                        croppedWidth = width,
                        croppedHeight = height
                    )
                )
            }
        }
    }
}
