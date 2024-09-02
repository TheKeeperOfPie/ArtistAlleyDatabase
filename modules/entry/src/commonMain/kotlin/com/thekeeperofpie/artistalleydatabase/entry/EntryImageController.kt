package com.thekeeperofpie.artistalleydatabase.entry

import androidx.annotation.WorkerThread
import androidx.compose.runtime.mutableStateListOf
import artistalleydatabase.modules.utils_compose.generated.resources.error_fail_to_load_image
import com.benasher44.uuid.Uuid
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.image.ImageHandler
import com.thekeeperofpie.artistalleydatabase.image.crop.CropState
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
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.jetbrains.compose.resources.StringResource

class EntryImageController(
    private val scope: CoroutineScope,
    private val appFileSystem: AppFileSystem,
    private val scopedIdType: String,
    private val onError: (Pair<StringResource, Throwable?>) -> Unit,
    onImageSizeResult: (Int, Int) -> Unit = { _, _ -> },
) {
    private var initialized = false

    private val entryIds = mutableListOf<EntryId>()

    var images = mutableStateListOf<EntryImage>()

    val imageState = EntryImageState(
        images = { images },
        onSelected = { index, uri -> onImageSelected(index, uri) },
        onSelectError = {
            onError(UtilsStrings.error_fail_to_load_image to it)
        },
        addAllowed = { entryIds.size <= 1 },
        onAdded = {
            scope.launch(Dispatchers.IO.limitedParallelism(8)) {
                val newImages = it.map {
                    async {
                        val (width, height) = appFileSystem.getImageWidthHeight(it)
                        EntryImage(
                            entryId = entryIds.singleOrNull(),
                            uri = it,
                            width = width ?: 1,
                            height = height ?: 1,
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

    fun initialize(entryIds: List<EntryId>) {
        if (initialized) return
        initialized = true
        this.entryIds += entryIds

        entryIds.firstOrNull()?.let {
            val entryImages =
                EntryUtils.getImages(appFileSystem, it)
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
                EntryUtils.getImages(appFileSystem, it)
            }
        }
    }

    fun onClickOpenImage(imageHandler: ImageHandler, index: Int) {
        val uri = images[index].run { croppedUri ?: uri } ?: return
        imageHandler.openImage(uri)
    }

    private fun onImageSelected(index: Int, uri: Uri?) {
        if (uri == null) {
            // TODO: Better image menu options
            return
        }

        scope.launch(Dispatchers.IO) {
            val (width, height) = appFileSystem.getImageWidthHeight(uri)
            width ?: return@launch
            height ?: return@launch
            withContext(Dispatchers.Main) {
                images[index] = images[index].copy(
                    uri = uri,
                    width = width,
                    height = height,
                    croppedUri = null,
                    croppedWidth = null,
                    croppedHeight = null,
                )
            }
        }
    }

    @WorkerThread
    suspend fun replaceMainImage(entryId: EntryId?, uri: Uri) {
        val entryIdsSize = withContext(Dispatchers.Main) { entryIds.size }
        if (entryIdsSize > 1) return

        val (width, height) = appFileSystem.getImageWidthHeight(uri)
        val newImage = EntryImage(
            entryId = entryId,
            uri = uri,
            width = width ?: 1,
            height = height ?: 1,
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
                && SystemFileSystem.metadataOrNull(entryFolder)?.isDirectory == true
            ) {
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

    fun onCropResult(result: CropState.CropResult) {
        val index = images.indexOfFirst { it.imageId == result.request.id }
        images[index] = images[index].copy(
            croppedUri = result.newImageUri,
            croppedWidth = result.croppedWidth,
            croppedHeight = result.croppedHeight,
        )
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
