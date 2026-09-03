package com.thekeeperofpie.artistalleydatabase.utils_preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider

class AlleyPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        MaterialTheme {
            Surface {
                content()
            }
        }
    }
}

@Preview
@Composable
@PreviewWrapper(wrapper = AlleyPreviewWrapper::class)
private fun AlleyPreviewWrapperPreview() {
    Text("Example text")
}
