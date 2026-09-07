package com.thekeeperofpie.artistalleydatabase.alley.models

actual object ImageUploadUtils {
    actual fun maxArtistUploadCount(isDebug: Boolean): Int = if (isDebug) 5 else 20
    actual fun maxStampRallyUploadCount(isDebug: Boolean): Int = if (isDebug) 2 else 20
}
