package com.thekeeperofpie.artistalleydatabase.alley.edit.images

import com.thekeeperofpie.artistalleydatabase.shared.alley.data.DatabaseImage
import com.thekeeperofpie.artistalleydatabase.utils.Bits

expect object ImageUtils {
    fun maxUploadSize(isDebug: Boolean): Bits

    fun toEditImage(isDebug: Boolean, catalogImage: DatabaseImage): EditImage
}
