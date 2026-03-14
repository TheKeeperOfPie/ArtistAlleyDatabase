package com.thekeeperofpie.artistalleydatabase.alley.models

actual object ImageUploadUtils {
    actual val MAX_ARTIST_UPLOAD_COUNT: Int = if (BuildKonfig.isWasmDebug) 5 else 20
    actual val MAX_STAMP_RALLY_UPLOAD_COUNT: Int = if (BuildKonfig.isWasmDebug) 2 else 5
}
