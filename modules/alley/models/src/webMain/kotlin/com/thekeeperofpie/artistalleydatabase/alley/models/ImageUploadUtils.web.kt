package com.thekeeperofpie.artistalleydatabase.alley.models

actual object ImageUploadUtils {
    actual val MAX_UPLOAD_COUNT: Int = if (BuildKonfig.isWasmDebug) 5 else 20
}
