@file:OptIn(ExperimentalCoilApi::class)

package com.thekeeperofpie.artistalleydatabase.utils_preview

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.hsl
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.tooling.preview.PreviewWrapperProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.ColorImage
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.compose.asPainter
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlin.random.Random

private class AlleyPreviewWrapper : PreviewWrapperProvider {
    @Composable
    override fun Wrap(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides CoilPreviewHandler) {
            MaterialTheme {
                Surface {
                    content()
                }
            }
        }
    }
}

private object CoilPreviewHandler : AsyncImagePreviewHandler {
    override suspend fun handle(
        imageLoader: ImageLoader,
        request: ImageRequest,
    ): AsyncImagePainter.State {
        val random = Random(request.data.hashCode())
        val color = hsl(
            hue = random.nextFloat() * 360f,
            saturation = lerp(0.5f, 0.8f, random.nextFloat()),
            lightness = lerp(0.5f, 0.8f, random.nextFloat()),
        )
        val image = ColorImage(color.toArgb())
        return AsyncImagePainter.State.Success(
            painter = image.asPainter(request.context),
            result = SuccessResult(image, request),
        )
    }
}

@Preview
@PreviewWrapper(wrapper = AlleyPreviewWrapper::class)
annotation class AlleyPreview

@AlleyPreview
@Composable
private fun AlleyPreviewWrapperPreview() {
    AsyncImage("test", null, modifier = Modifier.size(80.dp))
    Text("Example text")
}
