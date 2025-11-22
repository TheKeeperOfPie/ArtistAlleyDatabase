package com.thekeeperofpie.artistalleydatabase.utils_compose

import com.eygraber.uri.Uri
import kotlinx.io.files.Path

// No-op on desktop
actual class ShareHandler {
    actual fun shareUrl(title: String?, url: String) = Unit
    actual fun shareImage(path: Path?, uri: Uri?) = Unit
    actual fun shareText(text: String) = Unit
}
