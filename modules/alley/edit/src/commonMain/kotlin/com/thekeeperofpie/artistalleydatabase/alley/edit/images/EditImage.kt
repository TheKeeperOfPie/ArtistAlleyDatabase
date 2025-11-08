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

    data class Diff(
        val added: List<EditImage>,
        val deleted: List<EditImage>,
        val moved: List<Pair<IndexChange, EditImage>>,
    ) {
        data class IndexChange(val fromIndex: Int, val toIndex: Int)
    }

    companion object {
        fun generateDiffs(before: List<EditImage>, after: List<EditImage>): Diff {
            val (kept, added) = after.partition { it in before }
            val moved = kept.mapNotNull {
                val fromIndex = before.indexOf(it)
                val toIndex = after.indexOf(it)
                if (fromIndex == toIndex) {
                    null
                } else {
                    Diff.IndexChange(fromIndex, toIndex) to it
                }
            }

            val deleted = before.filter { it !in after }
            return Diff(added = added, deleted = deleted, moved = moved)
        }
    }
}
