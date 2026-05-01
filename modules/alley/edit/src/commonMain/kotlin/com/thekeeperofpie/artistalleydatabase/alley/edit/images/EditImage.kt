package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.util.toImageBitmap
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface EditImage : ImageWithDimensions {

    val name: String
    override val width: Int? get() = null
    override val height: Int? get() = null

    fun toCatalogImage() = if (this is LocalImage) {
        DatabaseImage(
            name = key.value.toString(),
            width = width,
            height = height,
        )
    } else {
        DatabaseImage(name, width, height)
    }

    @Serializable
    data class LocalImage(
        val key: PlatformImageKey,
        override val name: String,  
        val extension: String,
        val id: Uuid = Uuid.random(),
        override val width: Int? = null,
        override val height: Int? = null,
    ) : EditImage {
        companion object {
            suspend operator fun invoke(key: PlatformImageKey, file: PlatformFile): LocalImage {
                val imageBitmap = file.toImageBitmap()
                return LocalImage(
                    key = key,
                    name = file.name.ifBlank { key.value.toString() },
                    extension = file.extension,
                    width = imageBitmap.width,
                    height = imageBitmap.height,
                )
            }
        }

        override val coilImageModel: PlatformImageKey get() = key
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
