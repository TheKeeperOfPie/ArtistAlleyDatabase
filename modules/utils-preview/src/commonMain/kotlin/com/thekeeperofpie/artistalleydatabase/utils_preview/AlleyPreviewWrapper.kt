package com.thekeeperofpie.artistalleydatabase.utils_preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider

private class AlleyPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        // Roborazzi doesn't support automatically setting this
        CompositionLocalProvider(LocalInspectionMode provides true) {
            MaterialTheme {
                Surface {
                    content()
                }
            }
        }
    }
}

@Preview
@PreviewWrapper(wrapper = AlleyPreviewWrapper::class)
annotation class AlleyPreview

@AlleyPreview
@Composable
private fun AlleyPreviewWrapperPreview() {
    Text("Example text")
}
