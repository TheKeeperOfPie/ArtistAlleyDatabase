package com.thekeeperofpie.artistalleydatabase.entry

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import artistalleydatabase.modules.utils_compose.generated.resources.error_fail_to_crop_image
import artistalleydatabase.modules.utils_compose.generated.resources.error_fail_to_load_image
import com.benasher44.uuid.Uuid
import com.eygraber.uri.toAndroidUri
import com.eygraber.uri.toUri
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.walk
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.asInputStream
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.jetbrains.compose.resources.StringResource
import java.io.File

class EntryImageController(
    private val scopeProvider: () -> CoroutineScope,
    private val application: Application,
    private val appFileSystem: AppFileSystem,
    private val settings: EntrySettings,
    private val scopedIdType: String,
    private val onError: (Pair<StringResource, Throwable?>) -> Unit,
    @StringRes private val imageContentDescriptionRes: Int,
    onImageSizeResult: (Int, Int) -> Unit = { _, _ -> },
) {

    companion object {
        private const val TAG = "EntryImageController"
    }

    private var initialized = false

    private val entryIds = mutableListOf<EntryId>()

    var images = mutableStateListOf<EntryImage>()

    val imageState = EntryImageState(
        images = { images },
        onSelected = ::onImageSelected,
        onSelectError = {
            onError(UtilsStrings.error_fail_to_load_image to it)
        },
        addAllowed = { entryIds.size <= 1 },
        onAdded = {
            scopeProvider().launch(Dispatchers.IO.limitedParallelism(8)) {
                val newImages = it.map {
                    async {
                        val (width, height) = appFileSystem.getImageWidthHeight(it.toUri())
                        EntryImage(
                            entryId = entryIds.singleOrNull(),
                            uri = it.toUri(),
                            width = width ?: 1,
                            height = height ?: 1,
                            contentDescriptionRes = imageContentDescriptionRes,
                        )
                    }
                }.awaitAll()
                withContext(Dispatchers.Main) {
                    images += newImages
                }
            }
        },
        onSizeResult = onImageSizeResult,
    )

    private var imageCropUri by mutableStateOf<Uri?>(null)
    private var cropDocumentRequestedIndex by mutableIntStateOf(-1)
    private var cropReadyIndex by mutableIntStateOf(-1)

    val cropState = CropUtils.CropState(
        imageCropNeedsDocument = { imageCropUri == null },
        onImageCropDocumentChosen = ::onImageCropDocumentChosen,
        onImageRequestCrop = ::onImageRequestCrop,
        onCropFinished = ::onCropFinished,
        cropReadyIndex = { cropReadyIndex },
        onCropConfirmed = { cropDocumentRequestedIndex = it },
        cropDocumentRequestedIndex = { cropDocumentRequestedIndex },
    )

    /** Shared utility to easily signal URI invalidation by appending a query param of this value */
    private var invalidateIteration = 0

    fun initialize(entryIds: List<EntryId>) {
        if (initialized) return
        initialized = true
        this.entryIds += entryIds

        entryIds.firstOrNull()?.let {
            val entryImages =
                EntryUtils.getImages(appFileSystem, it, imageContentDescriptionRes)
                    .toMutableList()

            val firstImage = entryImages.firstOrNull()
            val firstUri = firstImage?.uri
            if (firstUri != null) {
                if (firstImage.width == 1) {
                    val (width, height) = appFileSystem.getImageWidthHeight(firstUri)
                    if (width != null && height != null) {
                        entryImages[0] = firstImage.copy(width = width, height = height)
                    }
                }
            }

            images += entryImages
            images += entryIds.drop(1).flatMap {
                EntryUtils.getImages(appFileSystem, it, imageContentDescriptionRes)
            }
        }

        scopeProvider().launch(Dispatchers.IO) {
            val serializedUri = settings.cropImageUri.value ?: return@launch
            try {
                val uri = Uri.parse(serializedUri)
                application.contentResolver.openOutputStream(uri)?.close()?.run {
                    launch(Dispatchers.Main) {
                        imageCropUri = uri
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading crop URI: $serializedUri")
            }
        }
    }

    fun onImageClickOpen(navHostController: NavHostController, index: Int) {
        val uri = images[index].run { croppedUri ?: uri } ?: return
        val path = uri.path
        if (uri.scheme == ContentResolver.SCHEME_FILE && path != null) {
            // TODO: Make this more reliable?
            EntryUtils.openInternalImage(navHostController, File(path))
        } else {
            EntryUtils.openImage(navHostController, uri.toAndroidUri())
        }
    }

    private fun onImageSelected(index: Int, uri: Uri?) {
        if (uri == null) {
            // TODO: Better image menu options
            return
        }

        scopeProvider().launch(Dispatchers.IO) {
            val inputStream = application.contentResolver.openInputStream(uri) ?: return@launch
            val (width, height) = inputStream.use {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(it, null, options)
                options.outHeight to options.outWidth
            }

            withContext(Dispatchers.Main) {
                images[index] = images[index].copy(
                    uri = uri.toUri(),
                    width = width,
                    height = height,
                    croppedUri = null,
                    croppedWidth = null,
                    croppedHeight = null,
                )
            }
        }
    }

    private fun onImageCropDocumentChosen(index: Int, uri: Uri?) {
        uri ?: return
        val imageUri = images.getOrNull(index)?.uri ?: return
        scopeProvider().launch(Dispatchers.IO) {
            try {
                application.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error persisting URI grant $uri")
                launch(Dispatchers.Main) {
                    onError(UtilsStrings.error_fail_to_crop_image to e)
                }
                return@launch
            }

            settings.cropImageUri.value = uri.toString()
            appFileSystem.openUri(imageUri)
                ?.use { input ->
                    application.contentResolver.openOutputStream(uri)?.use { output ->
                        input.asInputStream().copyTo(output)
                    }
                }?.run {
                    launch(Dispatchers.Main) {
                        imageCropUri = uri
                        cropReadyIndex = index
                    }
                }
        }
    }

    private fun onImageRequestCrop(index: Int) {
        val imageCropUri = imageCropUri ?: return
        val imageUri = images.getOrNull(index)?.uri ?: return
        scopeProvider().launch(Dispatchers.IO) {
            appFileSystem.openUri(imageUri)
                ?.use { input ->
                    application.contentResolver.openOutputStream(imageCropUri)?.use { output ->
                        input.asInputStream().copyTo(output)
                    }
                }

            application.grantUriPermission(
                CropUtils.PHOTOS_PACKAGE_NAME,
                imageCropUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            launch(Dispatchers.Main) {
                cropReadyIndex = index
            }
        }
    }

    private fun onCropFinished(index: Int?) {
        index ?: return
        val imageCropUri = imageCropUri ?: return
        scopeProvider().launch(Dispatchers.IO) {
            val entryId =
                images[index].entryId ?: EntryId(scopedIdType, Uuid.randomUUID().toString())
            val outputFile =
                EntryUtils.getCropTempFile(application, entryId, index)
            application.contentResolver.openInputStream(imageCropUri)?.use { input ->
                outputFile.outputStream().use { output -> input.copyTo(output) }
            }

            // Clear the temporary crop file contents
            application.contentResolver.openOutputStream(imageCropUri, "wt")?.use {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                    .compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            val (width, height) = outputFile.inputStream().use {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(it, null, options)
                options.outWidth to options.outHeight
            }

            val uri = outputFile.toUri()
                .buildUpon()
                .appendQueryParameter(
                    "invalidateIteration",
                    invalidateIteration++.toString()
                )
                .build()

            launch(Dispatchers.Main) {
                images[index] = images[index].copy(
                    entryId = entryId,
                    croppedUri = uri.toUri(),
                    croppedWidth = width,
                    croppedHeight = height,
                )
                cropReadyIndex = -1
            }
        }
    }

    @WorkerThread
    suspend fun replaceMainImage(entryId: EntryId?, uri: Uri) {
        val entryIdsSize = withContext(Dispatchers.Main) { entryIds.size }
        if (entryIdsSize > 1) return

        val (width, height) = appFileSystem.getImageWidthHeight(uri.toUri())
        val newImage = EntryImage(
            entryId = entryId,
            uri = uri.toUri(),
            width = width ?: 1,
            height = height ?: 1,
            contentDescriptionRes = imageContentDescriptionRes,
        )

        withContext(Dispatchers.Main) {
            if (images.isNotEmpty()) {
                images[0] = newImage
            } else {
                images += newImage
            }
        }
    }

    /**
     * @return List of files written.
     */
    suspend fun saveImages(): Map<EntryId, List<SaveResult>>? {
        val results = withContext(CustomDispatchers.io(4)) {
            images.mapIndexed { index, entryImage ->
                async {
                    // Generate a new ID for new entries
                    val entryId =
                        entryImage.entryId ?: EntryId(scopedIdType, Uuid.randomUUID().toString())
                    val originalPath = EntryUtils.getImagePath(
                        appFileSystem = appFileSystem,
                        entryId = entryId,
                        index = index,
                        width = entryImage.width,
                        height = entryImage.height,
                        label = entryImage.label,
                        cropped = false
                    )

                    val croppedPath = if (entryImage.croppedUri != null) {
                        EntryUtils.getImagePath(
                            appFileSystem = appFileSystem,
                            entryId = entryId,
                            index = index,
                            width = entryImage.croppedWidth ?: 1,
                            height = entryImage.croppedHeight ?: 1,
                            label = entryImage.label,
                            cropped = true
                        )
                    } else null

                    val originalError = appFileSystem.writeEntryImage(originalPath, entryImage.uri)
                    val croppedError = croppedPath?.let {
                        appFileSystem.writeEntryImage(
                            croppedPath,
                            entryImage.croppedUri
                        )
                    }

                    SaveResult(
                        entryId = entryId,
                        newEntryId = entryImage.entryId == null,
                        originalPath = originalPath.takeIf { originalError == null },
                        croppedPath = croppedPath.takeIf { croppedError == null },
                        result = croppedError ?: originalError,
                        width = entryImage.finalWidth,
                        height = entryImage.finalHeight,
                    )
                }
            }
                .awaitAll()
        }

        val result = results.find { it.result != null }?.result
        if (result != null && result.isFailure) {
            withContext(Dispatchers.Main) {
                onError(UtilsStrings.error_fail_to_load_image to result.exceptionOrNull())
            }
            return null
        }

        return results.groupBy { it.entryId }
    }

    fun cleanUpImages(
        entryIds: List<EntryId>,
        saveImagesResult: Map<EntryId, List<SaveResult>>,
    ) {
        entryIds.forEach {
            val entryFolder = EntryUtils.getEntryImageFolder(appFileSystem, it)
            if (SystemFileSystem.exists(entryFolder)
                && SystemFileSystem.metadataOrNull(entryFolder)?.isDirectory == true) {
                val writtenFiles = saveImagesResult[it]?.flatMap {
                    listOfNotNull(it.originalPath, it.croppedPath)
                }.orEmpty()
                SystemFileSystem.walk(entryFolder)
                    .filter { SystemFileSystem.metadataOrNull(it)?.isRegularFile == true }
                    .filterNot(writtenFiles::contains)
                    .forEach(SystemFileSystem::delete)
            }
        }
    }

    data class SaveResult(
        val entryId: EntryId,
        val newEntryId: Boolean,
        val originalPath: Path?,
        val croppedPath: Path?,
        val result: Result<*>?,
        val width: Int,
        val height: Int,
    )
}
