package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import kotlinx.serialization.Serializable

@Serializable
sealed interface EditImage : ImageWithDimensions {

    val name: String
    override val width: Int? get() = null
    override val height: Int? get() = null

    @Serializable
    data class DatabaseImage(
        val uri: Uri,
        override val width: Int?,
        override val height: Int?,
    ) : EditImage {
        override val name = uri.toString()
        override val coilImageModel: Uri get() = uri

        constructor(image: CatalogImage) : this(
            uri = image.uri,
            width = image.width,
            height = image.height,
        )
    }

    @Serializable
    data class LocalImage(val platformFile: PlatformFile) : EditImage {
        override val name = platformFile.name
        override val coilImageModel: PlatformFile get() = platformFile
    }

    @Serializable
    data class NetworkImage(val uri: Uri) : EditImage {
        override val name = uri.toString()
        override val coilImageModel: Uri get() = uri
    }
}
