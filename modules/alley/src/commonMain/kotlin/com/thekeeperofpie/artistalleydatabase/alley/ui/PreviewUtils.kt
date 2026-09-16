package com.thekeeperofpie.artistalleydatabase.alley.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler

@OptIn(ExperimentalCoilApi::class)
@Composable
fun PreviewDark(content: @Composable () -> Unit) {
    val previewHandler = AsyncImagePreviewHandler {
        ColorImage(it.data.hashCode())
    }

    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        MaterialTheme(colorScheme = darkColorScheme()) {
            Surface(content = content)
        }
    }
}

@Suppress("ComposableNaming")
@Composable
fun assertInPreview() {
    if (!LocalInspectionMode.current) {
        throw IllegalStateException("Must be in Compose preview")
    }
}
