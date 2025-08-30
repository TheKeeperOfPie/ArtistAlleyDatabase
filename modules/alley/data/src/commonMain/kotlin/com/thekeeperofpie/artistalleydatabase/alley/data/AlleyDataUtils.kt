package com.thekeeperofpie.artistalleydatabase.alley.data

import artistalleydatabase.modules.alley.data.generated.resources.Res
import com.eygraber.uri.Uri
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
}
