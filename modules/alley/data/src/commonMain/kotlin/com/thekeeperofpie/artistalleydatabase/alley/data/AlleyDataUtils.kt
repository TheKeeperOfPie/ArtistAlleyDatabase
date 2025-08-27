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
        artistId: String,
    ): List<CatalogImage> = getArtistImagesWithoutFallback(year, artistId)

    fun getArtistImages(
        year: DataYear,
        images: List<com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage>
    ) = images.map {
        val path = "files/${year.folderName}/${Folder.CATALOGS.folderName}/${it.name}"
        CatalogImage(
            uri = Uri.parse(Res.getUri(path)),
            width = it.width,
            height = it.height,
        )
    }

    fun getArtistImagesFallback(
        year: DataYear,
        artistId: String,
    ): Pair<DataYear, List<CatalogImage>>? =
        DataYear.entries.asReversed()
            .dropWhile { it != year }
            .firstNotNullOfOrNull { year ->
                getArtistImagesWithoutFallback(year, artistId)
                    .ifEmpty { null }
                    ?.let { year to it }
            }

    private fun getArtistImagesWithoutFallback(
        year: DataYear,
        artistId: String,
    ): List<CatalogImage> {
        val folder = when (year) {
            DataYear.ANIME_EXPO_2023 -> ComposeFiles.catalogs2023
            DataYear.ANIME_EXPO_2024 -> ComposeFiles.catalogs2024
            DataYear.ANIME_EXPO_2025 -> ComposeFiles.catalogs2025
            DataYear.ANIME_NYC_2024 -> ComposeFiles.catalogsAnimeNyc2024
            DataYear.ANIME_NYC_2025 -> ComposeFiles.catalogsAnimeNyc2025
        }[artistId]

        @OptIn(ExperimentalResourceApi::class)
        return folder?.files
            ?.filterIsInstance<ComposeFile.Image>()
            ?.sortedBy { it.name }
            ?.map {
                CatalogImage(
                    uri = Uri.parse(Res.getUri("files/${year.folderName}/${Folder.CATALOGS.folderName}/${folder.name}/${it.name}")),
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
            DataYear.ANIME_NYC_2025,
                -> id
        }

        // TODO: Rename rally folders to also allow access by key
        val targetFolder = when (year) {
            DataYear.ANIME_EXPO_2023 -> ComposeFiles.rallies2023
            DataYear.ANIME_EXPO_2024 -> ComposeFiles.rallies2024
            DataYear.ANIME_EXPO_2025 -> ComposeFiles.rallies2025
            DataYear.ANIME_NYC_2024 -> emptyMap()
            DataYear.ANIME_NYC_2025 -> emptyMap()
        }.values.find { it.name.startsWith(targetName) }

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
