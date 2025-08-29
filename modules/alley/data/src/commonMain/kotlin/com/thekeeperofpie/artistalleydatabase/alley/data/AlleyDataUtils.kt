package com.thekeeperofpie.artistalleydatabase.alley.data

import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFile
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFiles
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable

object AlleyDataUtils {

    @Serializable
    enum class Folder(val folderName: String) {
        CATALOGS("catalogs"),
        RALLIES("rallies"),
    }

    fun getArtistImages(
        year: DataYear,
        images: List<com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage>
    ) = images.map {
        val path = "files/${year.folderName}/catalogs/${it.name}"
        CatalogImage(
            uri = Uri.parse(Res.getUri(path)),
            width = it.width,
            height = it.height,
        )
    }

    fun getRallyImages(
        year: DataYear,
        images: List<com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage>
    ) = images.map {
        val path = "files/${year.folderName}/rallies/${it.name}"
        CatalogImage(
            uri = Uri.parse(Res.getUri(path)),
            width = it.width,
            height = it.height,
        )
    }

    fun exists(path: String): Boolean {
        val parts = path.substringAfter("generated.resources/files/").split("/")
        if (parts.size < 4) return false
        val yearFolderName = parts[0]
        val folderName = parts[1]
        val name = parts[2]
        val imageName = parts[3]

        val dataYear = DataYear.entries.find { it.folderName == yearFolderName } ?: return false
        val folderType = Folder.entries.find { it.folderName == folderName } ?: return false

        val targetFolder = when (dataYear) {
            DataYear.ANIME_EXPO_2023 -> when (folderType) {
                Folder.CATALOGS -> ComposeFiles.catalogs2023
                Folder.RALLIES -> ComposeFiles.rallies2023
            }
            DataYear.ANIME_EXPO_2024 -> when (folderType) {
                Folder.CATALOGS -> ComposeFiles.catalogs2024
                Folder.RALLIES -> ComposeFiles.rallies2024
            }
            DataYear.ANIME_EXPO_2025 -> when (folderType) {
                Folder.CATALOGS -> ComposeFiles.catalogs2025
                Folder.RALLIES -> ComposeFiles.rallies2025
            }
            DataYear.ANIME_NYC_2024 -> when (folderType) {
                Folder.CATALOGS -> ComposeFiles.catalogsAnimeNyc2024
                Folder.RALLIES -> emptyMap()
            }
            DataYear.ANIME_NYC_2025 -> when (folderType) {
                Folder.CATALOGS -> ComposeFiles.catalogsAnimeNyc2025
                Folder.RALLIES -> emptyMap()
            }
        }

        val fileFolder = targetFolder.values.find { it.name == name } ?: return false
        val imageFile = fileFolder.files.find { (it as? ComposeFile.Image)?.name == imageName }
        return imageFile != null
    }
}
