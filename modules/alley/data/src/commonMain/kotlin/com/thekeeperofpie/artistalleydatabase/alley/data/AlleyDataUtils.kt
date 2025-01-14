package com.thekeeperofpie.artistalleydatabase.alley.data

import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFile
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFiles
import org.jetbrains.compose.resources.ExperimentalResourceApi

object AlleyDataUtils {

    enum class Folder(val folderName: String) {
        CATALOGS("catalogs"),
        RALLIES("rallies"),
    }

    fun getImages(folder: Folder, file: String): List<CatalogImage> {
        val targetName = file.replace(" - ", "").replace("'", "_")
        val targetFolder = when (folder) {
            Folder.CATALOGS -> ComposeFiles.catalogs
            Folder.RALLIES -> ComposeFiles.rallies
        }.files
            .filterIsInstance<ComposeFile.Folder>()
            .find { it.name.startsWith(targetName) }

        @OptIn(ExperimentalResourceApi::class)
        return targetFolder?.files
            ?.filterIsInstance<ComposeFile.Image>()
            ?.sortedBy { it.name }
            ?.map {
                CatalogImage(
                    uri = Uri.parse(Res.getUri("files/${folder.folderName}/${targetFolder.name}/${it.name}")),
                    width = it.width,
                    height = it.height,
                )
            }
            .orEmpty()
    }
}
