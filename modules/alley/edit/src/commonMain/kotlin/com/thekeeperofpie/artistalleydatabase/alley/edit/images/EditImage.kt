package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DataYear
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface EditImage : ImageWithDimensions {

    val name: String
    override val width: Int? get() = null
    override val height: Int? get() = null

    fun toCatalogImage() = CatalogImage(name, width, height)

    @Serializable
    data class DatabaseImage(
        val uri: Uri,
        override val width: Int?,
        override val height: Int?,
        override val name: String = uri.toString(),
    ) : EditImage {
        override val coilImageModel: Uri get() = uri
    }

    @Serializable
    data class LocalImage(
        val key: PlatformImageKey,
        override val name: String,
        val extension: String,
        val id: Uuid = Uuid.random(),
    ) : EditImage {
        override val coilImageModel: PlatformImageKey get() = key

        constructor(key: PlatformImageKey, file: PlatformFile) : this(
            key = key,
            name = file.name.ifBlank { key.value.toString() },
            extension = file.extension,
        )

    }

    @Serializable
    data class NetworkImage(
        val uri: Uri,
        val key: String,
        override val width: Int? = null,
        override val height: Int? = null,
    ) : EditImage {
        override val name get() = key
        override val coilImageModel: Uri get() = uri

        companion object {
            fun makePrefix(dataYear: DataYear, id: String) = "${dataYear.serializedName}/$id"

            fun makeKey(
                dataYear: DataYear,
                entryId: Uuid,
                imageId: Uuid,
                platformFile: PlatformFile,
            ) = "${dataYear.serializedName}/$entryId/$imageId.${platformFile.extension}"
        }
    }

    fun fillSize(width: Int?, height: Int?): EditImage {
        if (width == null || height == null) return this
        return when (this) {
            is DatabaseImage -> copy(width = width, height = height)
            is LocalImage -> this
            is NetworkImage -> copy(width = width, height = height)
        }
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
