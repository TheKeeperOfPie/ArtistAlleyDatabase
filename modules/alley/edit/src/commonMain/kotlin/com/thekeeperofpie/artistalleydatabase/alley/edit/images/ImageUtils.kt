package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.utils.Bits

expect object ImageUtils {
    val MAX_UPLOAD_SIZE: Bits
    val MAX_UPLOAD_COUNT: Int

    fun toEditImage(catalogImage: CatalogImage): EditImage
}
