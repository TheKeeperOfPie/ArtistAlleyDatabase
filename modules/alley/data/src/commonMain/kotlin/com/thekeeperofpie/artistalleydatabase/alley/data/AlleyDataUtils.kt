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

    fun getImages(year: DataYear, folder: Folder, file: String?): List<CatalogImage> {
        file ?: return emptyList()
        val targetName = file.replace("'", "_")
        val targetFolder = when (year) {
            DataYear.YEAR_2023 -> when (folder) {
                Folder.CATALOGS -> ComposeFiles.catalogs2023
                Folder.RALLIES -> ComposeFiles.rallies2023
            }
            DataYear.YEAR_2024 -> when (folder) {
                Folder.CATALOGS -> ComposeFiles.catalogs2024
                Folder.RALLIES -> ComposeFiles.rallies2024
            }
            DataYear.YEAR_2025 -> when (folder) {
                Folder.CATALOGS -> ComposeFiles.catalogs2025
                Folder.RALLIES -> ComposeFiles.rallies2025
            }
        }.files
            .filterIsInstance<ComposeFile.Folder>()
            .find { it.name.startsWith(targetName) }

        @OptIn(ExperimentalResourceApi::class)
        return targetFolder?.files
            ?.filterIsInstance<ComposeFile.Image>()
            ?.sortedBy { it.name }
            ?.map {
                CatalogImage(
                    uri = Uri.parse(Res.getUri("files/${year.year}/${folder.folderName}/${targetFolder.name}/${it.name}")),
                    width = it.width,
                    height = it.height,
                )
            }
            .orEmpty()
    }
}
