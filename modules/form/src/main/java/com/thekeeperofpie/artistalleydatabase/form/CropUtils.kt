package com.thekeeperofpie.artistalleydatabase.form

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.thekeeperofpie.artistalleydatabase.form.CropUtils.CROP_IMAGE_FILE_NAME

/**
 * Reproducing an image crop UI within the app is both extremely difficult and ineffective, as any
 * attempt at an implementation will be much worse than the advanced cropping and editing features
 * allowed by third party apps such as Google Photos. Specifically perspective crop.
 *
 * However, Photos does not offer a crop `Intent` API for opening its `EditActivity`. The only way to
 * get Photos to crop an image successfully seems to be to either crop from the in-app library or
 * by passing a file:// URI which is readable/writable by both this app and Photos.
 *
 * This would generally be something in `/sdcard/Download` or similar. This requires several steps
 * of setup and prompts to the user.
 *
 * 1. User requests to crop an image (currently hidden as long press on entry image)
 * 1. Dialog pops explaining what they need to do in the following steps/screens
 * 1. If not previously set up for this app install, request [Intent.ACTION_CREATE_DOCUMENT]
 * pointing to `/sdcard/Download/`[CROP_IMAGE_FILE_NAME] (must be this exact path so that
 * a hardcoded `file://` URI will point to it), which creates a persistently read-writable image
 * file for the app to write an entry image to, which will then be passed to Photos to edit
 * 1. Using the URI from the previous step, invoke a callback up to the [ViewModel], which will
 * copy the content of the original image, `.../art_entry_images/ENTRY_UUID` to that document URI
 * 1. Send a completion notification to the Compose UI, which starts a
 * `com.android.camera.action.CROP`, targeting Photos's package name directly, passing it the
 * `file:///sdcard/Download/`[CROP_IMAGE_FILE_NAME] URI
 * 1. Receive the result and notify the [ViewModel], which will copy the result now stored at
 * [CROP_IMAGE_FILE_NAME]
 */
object CropUtils {

    const val CROP_IMAGE_FILE_NAME = "ArtistAlleyDatabaseImageCrop.png"
    const val PHOTOS_PACKAGE_NAME = "com.google.android.apps.photos"

    fun cropIntent(): Intent {
        val cropFileUri = Environment.getExternalStorageDirectory()
            .resolve("Download/$CROP_IMAGE_FILE_NAME").toUri()
        return Intent("com.android.camera.action.CROP")
            .setDataAndType(cropFileUri, "image/png")
            .putExtra("crop", "true")
            .putExtra("output", cropFileUri)
            .putExtra(
                "$PHOTOS_PACKAGE_NAME.editor.contract.explicit_output_type",
                "OVERWRITE"
            )
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    @Composable
    fun InstructionsDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.entry_image_crop_title)) },
            text = { Text(stringResource(R.string.entry_image_crop_text)) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onDismiss()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
        )
    }

    data class CropState(
        val imageCropNeedsDocument: () -> Boolean,
        val onImageCropDocumentChosen: (index: Int, Uri?) -> Unit = { _, _ -> },
        val onImageRequestCrop: (index: Int) -> Unit,
        val onCropFinished: (index: Int?) -> Unit,
        val cropReadyIndex: () -> Int,
        val onCropConfirmed: (index: Int) -> Unit,
        val cropDocumentRequestedIndex: () -> Int,
    )
}