package com.thekeeperofpie.artistalleydatabase.alley.images

import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

object AlleyImageUtils {

    fun getArtistImages(
        year: DataYear,
        images: List<CatalogImage>
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
        images: List<CatalogImage>
    ) = images.map {
        val path = "files/${year.folderName}/rallies/${it.name}"
        CatalogImage(
            uri = Uri.parse(Res.getUri(path)),
            width = it.width,
            height = it.height,
        )
    }

    suspend fun artistImageExists(artistEntryDao: ArtistEntryDao, path: String): Boolean {
        val parts = path.substringAfter("generated.resources/files/").split("/")
        if (parts.size < 3) return false
        val yearFolderName = parts[0]
        val name = parts[2]
        val imageName = parts[3]

        val dataYear = DataYear.entries.find { it.folderName == yearFolderName } ?: return false

        return artistEntryDao.getImagesById(dataYear, name.substringAfter("-").trim())
            ?.any { it.name.contains(imageName) }
            ?: false
    }
}
