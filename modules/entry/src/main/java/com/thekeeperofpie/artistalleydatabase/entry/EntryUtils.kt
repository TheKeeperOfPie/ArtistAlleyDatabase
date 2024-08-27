package com.thekeeperofpie.artistalleydatabase.entry

import android.content.Context
import android.content.Intent
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedElementComposable
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.toUri
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import java.io.File

object EntryUtils {

    const val SLIDE_DURATION_MS = 350

    data class ImageMetadata(
        val path: Path,
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
    fun getEntryImageFolder(appFileSystem: AppFileSystem, entryId: EntryId) =
        appFileSystem.filePath("${entryId.imageFolderName}/${entryId.valueId}")

    @WorkerThread
    fun getImages(
        appFileSystem: AppFileSystem,
        entryId: EntryId,
        @StringRes contentDescriptionRes: Int,
    ) = getEntryImageFolder(appFileSystem, entryId)
        .let {
            if (!SystemFileSystem.exists(it)) {
                emptyList()
            } else if (SystemFileSystem.metadataOrNull(it)?.isRegularFile == true) {
                listOf(
                    EntryImage(
                        entryId = entryId,
                        uri = it.toUri(),
                        width = 1,
                        height = 1,
                        contentDescriptionRes
                    )
                )
            } else if (SystemFileSystem.metadataOrNull(it)?.isDirectory == true) {
                SystemFileSystem.list(it)
                    .map {
                        // File are named $index-$width-$height-$label{-$cropped}
                        val sections = it.name.split("-")
                        ImageMetadata(
                            path = it,
                            index = sections.getOrNull(0)?.toIntOrNull() ?: -1,
                            width = sections.getOrNull(1)?.toIntOrNull() ?: 1,
                            height = sections.getOrNull(2)?.toIntOrNull() ?: 1,
                            label = sections.getOrNull(3) ?: "",
                            cropped = sections.getOrNull(4) == "cropped",
                        )
                    }
                    .groupBy { it.index }
                    .filterValues { it.first().index != -1 }
                    .mapValues {
                        val original = it.value.firstOrNull { !it.cropped }
                        val cropped = it.value.firstOrNull { it.cropped }
                        if (original == null) {
                            if (cropped == null) {
                                return@mapValues null
                            } else {
                                EntryImage(
                                    entryId = entryId,
                                    uri = cropped.path.toUri(),
                                    width = cropped.width,
                                    height = cropped.height,
                                    contentDescriptionRes = contentDescriptionRes,
                                    label = cropped.label,
                                )
                            }
                        } else {
                            EntryImage(
                                entryId = entryId,
                                uri = original.path.toUri(),
                                width = original.width,
                                height = original.height,
                                contentDescriptionRes = contentDescriptionRes,
                                label = original.label,
                                croppedUri = cropped?.path?.toUri(),
                                croppedWidth = cropped?.width,
                                croppedHeight = cropped?.height,
                            )
                        }
                    }
                    .toList()
                    .sortedBy { it.first }
                    .mapNotNull { it.second }
            } else {
                emptyList()
            }
        }

    fun getImagePath(appFileSystem: AppFileSystem, entryId: EntryId) =
        getEntryImageFolder(appFileSystem, entryId)
            .let {
                if (!SystemFileSystem.exists(it)) {
                    null
                } else if (SystemFileSystem.metadataOrNull(it)?.isRegularFile == true) {
                    it
                } else if (SystemFileSystem.metadataOrNull(it)?.isDirectory == true) {
                    val files = SystemFileSystem.list(it)
                    files.filter { it.name.split("-").getOrNull(0) == "0" }
                        .firstOrNull { it.name.endsWith("cropped") }
                        ?: files.firstOrNull()
                } else null
            }
            ?: getEntryImageFolder(appFileSystem, entryId)
                .takeIf { SystemFileSystem.metadataOrNull(it)?.isDirectory == true }
                ?.let {
                    val files = SystemFileSystem.list(it)
                    files.find { it.name == "0-1-1" } ?: files.firstOrNull()
                }

    fun getImagePath(
        appFileSystem: AppFileSystem,
        entryId: EntryId,
        index: Int,
        width: Int,
        height: Int,
        label: String,
        cropped: Boolean,
    ) = appFileSystem.filePath(
        ("${entryId.imageFolderName}$SystemPathSeparator" +
                "${entryId.valueId}$SystemPathSeparator" +
                "$index-$width-$height-$label")
            .let { if (cropped) "$it-cropped" else it }
    )

    // TODO: Store cropped images alongside originals instead of replacing
    fun getCropTempFile(context: Context, entryId: EntryId, index: Int) =
        context.filesDir
            .resolve("${entryId.imageFolderName}/crop/")
            .apply { mkdirs() }
            .resolve("${entryId.valueId}-$index")

    fun NavGraphBuilder.entryDetailsComposable(
        route: String,
        block: @Composable (entryIds: List<String>, imageCornerDp: Dp?) -> Unit,
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
    fun openImage(navHostController: NavHostController, uri: android.net.Uri) {
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

    fun fixImageName(appFileSystem: AppFileSystem, path: Path) {
        val (width, height) = appFileSystem.getImageWidthHeight(path.toUri())
        val newPath =
            Path(path.parent!!.toString() + SystemPathSeparator + "0-${width ?: 1}-${height ?: 1}")
        SystemFileSystem.atomicMove(path, newPath)
    }

    fun getImageCacheKey(it: EntryImage, width: Int, height: Int) =
        "coil_memory_entry_image_home_${it.entryId?.scopedId}_${width}_$height"

    fun getImageCacheKey(it: EntryGridModel, width: Int, height: Int) =
        "coil_memory_entry_image_home_${it.id.scopedId}_${width}_$height"
}
