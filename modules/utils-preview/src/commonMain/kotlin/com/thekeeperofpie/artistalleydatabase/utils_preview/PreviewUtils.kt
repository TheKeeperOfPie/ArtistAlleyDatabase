package com.thekeeperofpie.artistalleydatabase.utils_preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler

@Suppress("ComposableNaming")
@Composable
fun assertInPreview() {
    if (!LocalInspectionMode.current) {
        throw IllegalStateException("Must be in Compose preview")
    }
}
