package com.thekeeperofpie.artistalleydatabase.utils_compose.image

import coil3.request.ImageRequest
import coil3.request.allowHardware

actual fun ImageRequest.Builder.allowHardware(allowHardware: Boolean) =
    allowHardware(enable = allowHardware)
