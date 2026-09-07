package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.utils.AlleyUtils
import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import com.thekeeperofpie.artistalleydatabase.utils.megabytes
import kotlinx.browser.window

actual object ImageUtils {
    actual fun maxUploadSize(isDebug: Boolean) = if (isDebug) 5.megabytes else 10.megabytes

    actual fun toEditImage(isDebug: Boolean, catalogImage: DatabaseImage): EditImage {
        val key = catalogImage.name
        return EditImage.NetworkImage(
            uri = Uri.parse(
                if (isDebug) {
                    "${window.origin}/edit/api/image"
                } else {
                    AlleyUtils.prodImagesUrl
                } + "/$key"
            ),
            key = key,
        )
    }
}
