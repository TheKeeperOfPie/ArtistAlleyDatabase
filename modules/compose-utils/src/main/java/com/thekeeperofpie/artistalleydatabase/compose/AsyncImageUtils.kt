@file:OptIn(ExperimentalSerializationApi::class)

package com.thekeeperofpie.artistalleydatabase.compose

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.SingletonImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.DefaultModelEqualityDelegate
import coil3.compose.EqualityDelegate
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

fun MemoryCache.Key.toImageCacheKey() = ImageCacheKey(this.key, this.extras)
fun ImageCacheKey.toMemoryCacheKey() = MemoryCache.Key(this.key, this.extras)

@Parcelize
@Serializable
data class ImageCacheKey(val key: String, val extras: Map<String, String>) : Parcelable

@Parcelize
@Serializable
data class ImageState(
    val uri: String?,
    val cacheKey: ImageCacheKey? = null,
) : Parcelable

fun ImageState?.maybeOverride(uri: String?) = if (uri == null) {
    this
} else {
    this?.copy(uri = uri)
} ?: ImageState(uri)

// TODO: This recomposes when `uri` changes, which might affect the top level composable rather
//  than the user of the state. Could maybe push the read further down?
@Composable
fun rememberCoilImageState(
    uri: String?,
    cacheKey: ImageCacheKey? = null,
) = remember(uri) { CoilImageState(uri, cacheKey) }

@Composable
fun rememberCoilImageState(imageState: ImageState?) =
    rememberCoilImageState(uri = imageState?.uri, cacheKey = imageState?.cacheKey)

class CoilImageState internal constructor(
    val uri: String?,
    var imageCacheKey: ImageCacheKey? = null,
) {
    fun toImageState() = ImageState(uri = uri, cacheKey = imageCacheKey)
}

@Composable
fun CoilImageState?.request() = ImageRequest.Builder(LocalPlatformContext.current)
    .data(this?.uri)
    .placeholderMemoryCacheKey(this?.imageCacheKey?.toMemoryCacheKey())

@Composable
@NonRestartableComposable
fun CoilImage(
    state: CoilImageState?,
    model: ImageRequest?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = error,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    clipToBounds: Boolean = true,
    modelEqualityDelegate: EqualityDelegate = DefaultModelEqualityDelegate,
) = coil3.compose.AsyncImage(
    model = model,
    contentDescription = contentDescription,
    imageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
    modifier = modifier,
    placeholder = placeholder,
    error = error,
    fallback = fallback,
    onLoading = onLoading,
    onSuccess = {
        state?.imageCacheKey = it.result.memoryCacheKey?.toImageCacheKey()
        onSuccess?.invoke(it)
    },
    onError = onError,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
    clipToBounds = clipToBounds,
    modelEqualityDelegate = modelEqualityDelegate,
)
