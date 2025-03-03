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

    fun exists(path: String): Boolean {
        val parts = path.substringAfter("generated.resources/files/").split("/")
        if (parts.size < 4) return false
        val year = parts[0].toIntOrNull()
        val folderName = parts[1]
        val name = parts[2]
        val imageName = parts[3]

        val dataYear = DataYear.entries.find { it.year == year } ?: return false
        val folderType = Folder.entries.find { it.folderName == folderName } ?: return false

        val targetFolder = when (dataYear) {
            DataYear.YEAR_2023 -> when (folderType) {
                Folder.CATALOGS -> ComposeFiles.catalogs2023
                Folder.RALLIES -> ComposeFiles.rallies2023
            }
            DataYear.YEAR_2024 -> when (folderType) {
                Folder.CATALOGS -> ComposeFiles.catalogs2024
                Folder.RALLIES -> ComposeFiles.rallies2024
            }
            DataYear.YEAR_2025 -> when (folderType) {
                Folder.CATALOGS -> ComposeFiles.catalogs2025
                Folder.RALLIES -> ComposeFiles.rallies2025
            }
        }

        val fileFolder = targetFolder.files
            .filterIsInstance<ComposeFile.Folder>()
            .find { it.name == name }
            ?: return false

        val imageFile = fileFolder.files.find { (it as? ComposeFile.Image)?.name == imageName }
        return imageFile != null
    }
}
