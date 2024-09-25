package com.thekeeperofpie.artistalleydatabase.utils_compose

import androidx.compose.runtime.compositionLocalOf

expect class ShareHandler {
    fun shareUrl(title: String?, url: String)
}

val LocalShareHandler = compositionLocalOf<ShareHandler?> { null }
