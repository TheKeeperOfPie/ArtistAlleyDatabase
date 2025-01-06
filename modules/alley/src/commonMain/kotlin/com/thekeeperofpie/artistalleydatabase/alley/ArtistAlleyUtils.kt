package com.thekeeperofpie.artistalleydatabase.alley

import artistalleydatabase.modules.alley.generated.resources.Res
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFile
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFiles
import com.thekeeperofpie.artistalleydatabase.utils.io.AppFileSystem
import org.jetbrains.compose.resources.ExperimentalResourceApi

object ArtistAlleyUtils {

    fun getImages(
        appFileSystem: AppFileSystem,
        folder: String,
        file: String,
    ): List<CatalogImage> {
        val targetName = file.replace(" - ", "").replace("'", "_")
        val targetFolder = ComposeFiles.root
            .filterIsInstance<ComposeFile.Folder>()
            .find { it.name == folder }
            ?.files
            ?.filterIsInstance<ComposeFile.Folder>()
            ?.find { it.name.startsWith(targetName) }

        @OptIn(ExperimentalResourceApi::class)
        return targetFolder?.files
            ?.filterIsInstance<ComposeFile.Image>()
            ?.sortedBy { it.name }
            ?.map {
                CatalogImage(
                    uri = Uri.parse(Res.getUri("files/$folder/${targetFolder.name}/${it.name}")),
                    width = it.width,
                    height = it.height,
                )
            }
            .orEmpty()
    }
}
