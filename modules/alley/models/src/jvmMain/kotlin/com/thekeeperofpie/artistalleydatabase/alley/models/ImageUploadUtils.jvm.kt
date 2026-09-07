package com.thekeeperofpie.artistalleydatabase.alley.models

actual object ImageUploadUtils {
    actual fun maxArtistUploadCount(isDebug: Boolean) = 5
    actual fun maxStampRallyUploadCount(isDebug: Boolean) = 5
}
