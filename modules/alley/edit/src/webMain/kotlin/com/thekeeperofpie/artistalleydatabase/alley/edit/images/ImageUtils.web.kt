package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.edit.secrets.BuildKonfig
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.utils.megabytes
import kotlinx.browser.window

actual object ImageUtils {
    actual val MAX_UPLOAD_SIZE = 20.megabytes

    actual fun toEditImage(catalogImage: CatalogImage): EditImage {
        val key = catalogImage.name
        return EditImage.NetworkImage(
            uri = Uri.parse(
                BuildKonfig.imagesUrl.ifBlank { "${window.origin}/edit/api/image" } + "/$key"
            ),
            key = key,
        )
    }
}
