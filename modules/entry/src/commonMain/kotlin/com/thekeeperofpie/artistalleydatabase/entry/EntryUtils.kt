package com.thekeeperofpie.artistalleydatabase.entry

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.thekeeperofpie.artistalleydatabase.entry.grid.EntryGridModel
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import com.thekeeperofpie.artistalleydatabase.utils.io.toUri
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElementComposable
import kotlinx.io.files.Path
import kotlinx.io.files.SystemPathSeparator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object EntryUtils {

    data class ImageMetadata(
        val path: Path,
        val index: Int,
        val width: Int,
        val height: Int,
        val label: String,
        val cropped: Boolean,
    )

    // TODO: Remove this and migrate the folder naming?
    val EntryId.imageFolderName: String
        get() = when (type) {
            "art_entry" -> "art_entry_images"
            "cd_entry" -> "cd_entry_images"
            "test" -> "test_entry_images"
            else -> throw IllegalArgumentException("Unrecognized type $type")
        }

    fun getEntryImageFolder(appFileSystem: AppFileSystem, entryId: EntryId) =
        appFileSystem.filePath("${entryId.imageFolderName}$SystemPathSeparator${entryId.valueId}")

    fun getImages(
        appFileSystem: AppFileSystem,
        entryId: EntryId,
    ) = getEntryImageFolder(appFileSystem, entryId)
        .let {
            if (!appFileSystem.exists(it)) {
                emptyList()
            } else if (appFileSystem.metadataOrNull(it)?.isRegularFile == true) {
                listOf(
                    @OptIn(ExperimentalUuidApi::class)
                    EntryImage(
                        entryId = entryId,
                        imageId = Uuid.random().toString(),
                        uri = it.toUri(),
                        width = 1,
                        height = 1,
                    )
                )
            } else if (appFileSystem.metadataOrNull(it)?.isDirectory == true) {
                appFileSystem.list(it)
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
                                    label = cropped.label,
                                )
                            }
                        } else {
                            EntryImage(
                                entryId = entryId,
                                uri = original.path.toUri(),
                                width = original.width,
                                height = original.height,
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
                if (!appFileSystem.exists(it)) {
                    null
                } else if (appFileSystem.metadataOrNull(it)?.isRegularFile == true) {
                    it
                } else if (appFileSystem.metadataOrNull(it)?.isDirectory == true) {
                    val files = appFileSystem.list(it)
                    files.filter { it.name.split("-").getOrNull(0) == "0" }
                        .firstOrNull { it.name.endsWith("cropped") }
                        ?: files.firstOrNull()
                } else null
            }
            ?: getEntryImageFolder(appFileSystem, entryId)
                .takeIf { appFileSystem.metadataOrNull(it)?.isDirectory == true }
                ?.let {
                    val files = appFileSystem.list(it)
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
        val entryIds = it.arguments?.read { getStringArrayOrNull("entry_ids") }.orEmpty()
            .toList()
        val imageCornerDp = it.arguments?.read { getStringOrNull("image_corner_dp") }
            ?.toIntOrNull()
        block(entryIds, imageCornerDp?.dp)
    }

    fun NavHostController.navToEntryDetails(route: String, entryIds: List<String>) {
        var path = route
        if (entryIds.isNotEmpty()) {
            path += "?entry_ids=${entryIds.joinToString(separator = "&entry_ids=")}"
        }
        navigate(path)
    }

    fun fixImageName(appFileSystem: AppFileSystem, path: Path) {
        val (width, height) = appFileSystem.getImageWidthHeight(path.toUri())
        val newPath =
            Path(path.parent!!.toString() + SystemPathSeparator + "0-${width ?: 1}-${height ?: 1}")
        appFileSystem.atomicMove(path, newPath)
    }

    fun getImageCacheKey(it: EntryImage, width: Int, height: Int) =
        "coil_memory_entry_image_home_${it.entryId?.scopedId}_${width}_$height"

    fun getImageCacheKey(it: EntryGridModel, width: Int, height: Int) =
        "coil_memory_entry_image_home_${it.id.scopedId}_${width}_$height"
}
