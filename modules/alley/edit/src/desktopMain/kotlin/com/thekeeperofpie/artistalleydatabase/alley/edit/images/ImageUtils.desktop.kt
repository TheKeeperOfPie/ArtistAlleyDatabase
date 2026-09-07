package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import com.thekeeperofpie.artistalleydatabase.utils.megabytes
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@Inject
actual class ImageUtils {
    actual val maxUploadSize = 5.megabytes

    actual fun toEditImage(catalogImage: DatabaseImage): EditImage {
        val id = Uuid.parseOrNull(catalogImage.name)
        val key = id?.let(::PlatformImageKey)
        val file = key?.let(PlatformImageCache::get)
            ?: PlatformFile(catalogImage.name)
        return EditImage.LocalImage(
            key = key ?: PlatformImageCache.add(file),
            name = catalogImage.name,
            extension = file.extension,
            id = id ?: Uuid.random(),
            width = catalogImage.width,
            height = catalogImage.height,
        )
    }
}
