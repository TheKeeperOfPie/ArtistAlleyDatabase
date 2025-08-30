package com.thekeeperofpie.artistalleydatabase.alley.app

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear

object AlleyImageUtils {

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
