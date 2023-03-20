package com.thekeeperofpie.artistalleydatabase.entry

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.thekeeperofpie.artistalleydatabase.android_utils.ImageUtils
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import java.io.File

object EntryUtils {

    data class ImageMetadata(
        val file: File,
        val index: Int,
        val width: Int,
        val height: Int,
        val label: String,
        val cropped: Boolean,
    )

    // TODO: Remove this and migrate the folder naming?
    private val EntryId.imageFolderName: String
        get() = when (type) {
            "art_entry" -> "art_entry_images"
            "cd_entry" -> "cd_entry_images"
            "test" -> "test_entry_images"
            else -> throw IllegalArgumentException("Unrecognized type $type")
        }

    @WorkerThread
    fun getEntryImageFolder(context: Context, entryId: EntryId) =
        context.filesDir.resolve("${entryId.imageFolderName}/${entryId.valueId}")

    @WorkerThread
    fun getImages(
        context: Context,
        entryId: EntryId,
        @StringRes contentDescriptionRes: Int
    ) = getEntryImageFolder(context, entryId)
        .let {
            if (!it.exists()) {
                emptyList()
            } else if (it.isFile) {
                listOf(
                    EntryImage(
                        entryId = entryId,
                        uri = it.toUri(),
                        width = 1,
                        height = 1,
                        contentDescriptionRes
                    )
                )
            } else {
                it.listFiles()
                    ?.map {
                        // File are named $index-$width-$height-$label{-$cropped}
                        val sections = it.name.split("-")
                        ImageMetadata(
                            file = it,
                            index = sections.getOrNull(0)?.toIntOrNull() ?: -1,
                            width = sections.getOrNull(1)?.toIntOrNull() ?: 1,
                            height = sections.getOrNull(2)?.toIntOrNull() ?: 1,
                            label = sections.getOrNull(3) ?: "",
                            cropped = sections.getOrNull(4) == "cropped",
                        )
                    }
                    ?.groupBy { it.index }
                    ?.filterValues { it.first().index != -1 }
                    ?.mapValues {
                        val original = it.value.firstOrNull { !it.cropped }
                        val cropped = it.value.firstOrNull { it.cropped }
                        if (original == null) {
                            if (cropped == null) {
                                return@mapValues null
                            } else {
                                EntryImage(
                                    entryId = entryId,
                                    uri = cropped.file.toUri(),
                                    width = cropped.width,
                                    height = cropped.height,
                                    contentDescriptionRes = contentDescriptionRes,
                                    label = cropped.label,
                                )
                            }
                        } else {
                            EntryImage(
                                entryId = entryId,
                                uri = original.file.toUri(),
                                width = original.width,
                                height = original.height,
                                contentDescriptionRes = contentDescriptionRes,
                                label = original.label,
                                croppedUri = cropped?.file?.toUri(),
                                croppedWidth = cropped?.width,
                                croppedHeight = cropped?.height,
                            )
                        }
                    }
                    ?.toList()
                    ?.sortedBy { it.first }
                    ?.map { it.second }
                    ?.filterNotNull()
                    .orEmpty()
            }
        }

    fun getImageFile(context: Context, entryId: EntryId) =
        getEntryImageFolder(context, entryId)
            .let {
                if (!it.exists()) {
                    null
                } else if (it.isFile) {
                    it
                } else if (it.isDirectory) {
                    it.listFiles()
                        ?.filter {
                            it.name.split("-").getOrNull(0) == "0"
                        }
                        ?.firstOrNull { it.name.endsWith("cropped") }
                        ?: it.listFiles()?.firstOrNull()
                } else null
            }
            ?: getEntryImageFolder(context, entryId).resolve("0-1-1")

    fun getImageFile(
        context: Context,
        entryId: EntryId,
        index: Int,
        width: Int,
        height: Int,
        label: String,
        cropped: Boolean,
    ) =
        context.filesDir.resolve(("${entryId.imageFolderName}/${entryId.valueId}/" +
                "$index-$width-$height-$label")
            .let { if (cropped) "$it-cropped" else it })

    // TODO: Store cropped images alongside originals instead of replacing
    fun getCropTempFile(context: Context, entryId: EntryId, index: Int) =
        context.filesDir
            .resolve("${entryId.imageFolderName}/crop/")
            .apply { mkdirs() }
            .resolve("${entryId.valueId}-$index")

    fun NavGraphBuilder.entryDetailsComposable(
        route: String,
        block: @Composable (entryIds: List<String>) -> Unit
    ) = composable(
        "$route?entry_ids={entry_ids}",
        arguments = listOf(
            navArgument("entry_ids") {
                type = NavType.StringArrayType
                nullable = true
            },
        )
    ) {
        block(it.arguments?.getStringArray("entry_ids")?.toList() ?: emptyList())
    }

    fun NavHostController.navToEntryDetails(route: String, entryIds: List<String>) {
        var path = route
        if (entryIds.isNotEmpty()) {
            path += "?entry_ids=${entryIds.joinToString(separator = "&entry_ids=")}"
        }
        navigate(path)
    }

    @MainThread
    fun openInternalImage(navHostController: NavHostController, file: File) {
        val context = navHostController.context
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(imageUri, "image/*")
        }

        val chooserIntent = Intent.createChooser(
            intent,
            context.getString(R.string.entry_open_full_image_content_description)
        )
        context.startActivity(chooserIntent)
    }

    @MainThread
    fun openImage(navHostController: NavHostController, uri: Uri) {
        val context = navHostController.context
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(uri, "image/*")
        }

        val chooserIntent = Intent.createChooser(
            intent,
            context.getString(R.string.entry_open_full_image_content_description)
        )
        context.startActivity(chooserIntent)
    }

    fun fixImageName(context: Context, file: File) {
        val (width, height) = ImageUtils.getImageWidthHeight(context, file.toUri())
        file.renameTo(file.resolveSibling("0-${width ?: 1}-${height ?: 1}"))
    }

    // TODO: The cache keys don't account for crop state or image changes in general, so
    //  the wrong image is loaded whenever a crop/image change is saved
    fun getImageCacheKey(it: EntryImage) = "coil_memory_entry_image_home_${it.entryId?.scopedId}"

    fun getImageCacheKey(it: EntryGridModel) = "coil_memory_entry_image_home_${it.id.scopedId}"
}