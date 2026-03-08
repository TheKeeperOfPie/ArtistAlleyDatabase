package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.utils.megabytes
import io.github.vinceglb.filekit.PlatformFile

actual object ImageUtils {
    actual val MAX_UPLOAD_SIZE = 5.megabytes

    actual fun toEditImage(catalogImage: CatalogImage): EditImage {
        val file = PlatformFile(catalogImage.name)
        val key = PlatformImageCache.add(file)
        return EditImage.LocalImage(key, file)
    }
}
