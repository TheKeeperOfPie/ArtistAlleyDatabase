package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.compositionLocalOf
import com.eygraber.uri.Uri
import kotlinx.io.files.Path

expect class ShareHandler {
    fun shareUrl(title: String?, url: String)
    fun shareImage(path: Path?, uri: Uri?)
    fun shareText(text: String)
}

val LocalShareHandler = compositionLocalOf<ShareHandler?> { null }
