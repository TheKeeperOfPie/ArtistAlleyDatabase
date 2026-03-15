package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.utils.megabytes
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import kotlin.uuid.Uuid

actual object ImageUtils {
    actual val MAX_UPLOAD_SIZE = 5.megabytes

    actual fun toEditImage(catalogImage: CatalogImage): EditImage {
        val id = Uuid.parseOrNull(catalogImage.name)
        val key = id?.let(::PlatformImageKey)
        val file = key?.let(PlatformImageCache::get)
            ?: PlatformFile(catalogImage.name)
        return EditImage.LocalImage(
            key = key ?: PlatformImageCache.add(file),
            name = catalogImage.name,
            extension = file.extension,
            id = id ?: Uuid.random(),
        )
    }
}
