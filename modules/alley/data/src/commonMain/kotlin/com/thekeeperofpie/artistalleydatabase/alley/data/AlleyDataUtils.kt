package com.thekeeperofpie.artistalleydatabase.alley.data

import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFile
import com.thekeeperofpie.artistalleydatabase.generated.ComposeFiles
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.ExperimentalResourceApi

object AlleyDataUtils {

    @Serializable
    enum class Folder(val folderName: String) {
        CATALOGS("catalogs"),
        RALLIES("rallies"),
    }

    fun getArtistImages(
        year: DataYear,
        booth: String?,
        name: String?,
    ): List<CatalogImage> {
        booth ?: return emptyList()
        val targetName = fixName(booth)
        val candidates = when (year) {
            DataYear.ANIME_EXPO_2023 -> ComposeFiles.catalogs2023
            DataYear.ANIME_EXPO_2024 -> ComposeFiles.catalogs2024
            DataYear.ANIME_EXPO_2025 -> ComposeFiles.catalogs2025
            DataYear.ANIME_NYC_2024 -> ComposeFiles.catalogsAnimeNyc2024
            DataYear.ANIME_NYC_2025 -> ComposeFiles.catalogsAnimeNyc2025
        }.files
            .filterIsInstance<ComposeFile.Folder>()
            .filter { it.name.startsWith(targetName) }

        val targetFolder = if (year == DataYear.ANIME_EXPO_2023 && name != null) {
            findName2023(candidates, name)
        } else {
            candidates.firstOrNull()
        }

        @OptIn(ExperimentalResourceApi::class)
        return targetFolder?.files
            ?.filterIsInstance<ComposeFile.Image>()
            ?.sortedBy { it.name }
            ?.map {
                CatalogImage(
                    uri = Uri.parse(Res.getUri("files/${year.folderName}/${Folder.CATALOGS.folderName}/${targetFolder.name}/${it.name}")),
                    width = it.width,
                    height = it.height,
                )
            }
            .orEmpty()
    }

    fun getRallyImages(
        year: DataYear,
        id: String,
        hostTable: String?,
        fandom: String?,
    ): List<CatalogImage> {
        hostTable ?: fandom ?: return emptyList()
        val file = "$hostTable$fandom"
        val targetName = when (year) {
            DataYear.ANIME_EXPO_2023,
            DataYear.ANIME_EXPO_2024,
                -> fixName(file)
            DataYear.ANIME_EXPO_2025,
            DataYear.ANIME_NYC_2024,
            DataYear.ANIME_NYC_2025, -> id
        }
        val targetFolder = when (year) {
            DataYear.ANIME_EXPO_2023 -> ComposeFiles.rallies2023
            DataYear.ANIME_EXPO_2024 -> ComposeFiles.rallies2024
            DataYear.ANIME_EXPO_2025 -> ComposeFiles.rallies2025
            DataYear.ANIME_NYC_2024 -> ComposeFile.Folder("ralliesAnimeNyc2024", emptyList())
            DataYear.ANIME_NYC_2025 -> ComposeFile.Folder("ralliesAnimeNyc2025", emptyList())
        }.files
            .filterIsInstance<ComposeFile.Folder>()
            .find { it.name.startsWith(targetName) }

        @OptIn(ExperimentalResourceApi::class)
        return targetFolder?.files
            ?.filterIsInstance<ComposeFile.Image>()
            ?.sortedBy { it.name }
            ?.map {
                CatalogImage(
                    uri = Uri.parse(Res.getUri("files/${year.folderName}/${Folder.RALLIES.folderName}/${targetFolder.name}/${it.name}")),
                    width = it.width,
                    height = it.height,
                )
            }
            .orEmpty()
    }

    fun findName2023(folders: List<ComposeFile.Folder>, name: String): ComposeFile.Folder? {
        val escapedName = fixName(name).removeSuffix(".")
        val exact = folders.find { it.name.contains(escapedName, ignoreCase = true) }
        if (exact != null) return exact
        val segments = escapedName.split(Regex(" - ")).reversed()
        if (segments.isEmpty()) return null
        return folders.find { folder ->
            segments.any { segment ->
                folder.name.contains(segment, ignoreCase = true)
            }
        }
    }

    private fun fixName(name: String) = name.replace("'", "_")
        .replace("&", "_")

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
                Folder.RALLIES -> ComposeFile.Folder("ralliesAnimeNyc2024", emptyList())
            }
            DataYear.ANIME_NYC_2025 -> when (folderType) {
                Folder.CATALOGS -> ComposeFiles.catalogsAnimeNyc2025
                Folder.RALLIES -> ComposeFile.Folder("ralliesAnimeNyc2025", emptyList())
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
