package com.thekeeperofpie.artistalleydatabase.entry

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.thekeeperofpie.artistalleydatabase.android_utils.ImageUtils
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import java.io.File

object EntryUtils {

    const val SLIDE_DURATION_MS = 350

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
                    ?.mapNotNull { it.second }
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
        block: @Composable (entryIds: List<String>, imageCornerDp: Dp?) -> Unit
    ) = sharedElementComposable(
        "$route?entry_ids={entry_ids}&image_corner_dp={image_corner_dp}",
        arguments = listOf(
            navArgument("entry_ids") {
                type = NavType.StringArrayType
                nullable = true
            },
            navArgument("image_corner_dp") {
                type = NavType.StringType
                nullable = true
            },
        )
    ) {
        val entryIds = it.arguments?.getStringArray("entry_ids")?.toList() ?: emptyList()
        val imageCornerDp = it.arguments?.getString("image_corner_dp")?.toIntOrNull()
        block(entryIds, imageCornerDp?.dp)
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

    fun getImageCacheKey(it: EntryImage, width: Int, height: Int) =
        "coil_memory_entry_image_home_${it.entryId?.scopedId}_${width}_$height"

    fun getImageCacheKey(it: EntryGridModel, width: Int, height: Int) =
        "coil_memory_entry_image_home_${it.id.scopedId}_${width}_$height"
}
