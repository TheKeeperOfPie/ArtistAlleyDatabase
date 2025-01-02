package com.thekeeperofpie.artistalleydatabase.alley

import androidx.annotation.WorkerThread
import artistalleydatabase.modules.alley.generated.resources.Res
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFiles
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import org.jetbrains.compose.resources.ExperimentalResourceApi

object ArtistAlleyUtils {

    @WorkerThread
    fun getImages(
        appFileSystem: AppFileSystem,
        folder: String,
        file: String,
    ): List<CatalogImage> {
        val targetName = file.replace("'", "_")
        val targetFolder = ComposeFiles.folders
            .find { it.name == folder }
            ?.files
            ?.find { it.name.startsWith(targetName) }

        @OptIn(ExperimentalResourceApi::class)
        return targetFolder?.files
            ?.filterNot { it.name.endsWith(".pdf") }
            ?.sortedBy { it.name }
            ?.map { Uri.parse(Res.getUri("files/$folder/${targetFolder.name}/${it.name}")) }
            ?.map {
                val (width, height) = appFileSystem.getImageWidthHeight(it)
                CatalogImage(
                    uri = it,
                    width = width,
                    height = height,
                )
            }
            .orEmpty()
    }
}
