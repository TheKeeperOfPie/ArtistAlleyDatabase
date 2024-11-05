package com.thekeeperofpie.artistalleydatabase.utils_compose.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import coil3.SingletonImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import kotlinx.serialization.Serializable

fun MemoryCache.Key.toImageCacheKey() = ImageCacheKey(this.key, this.extras)
fun ImageCacheKey.toMemoryCacheKey() = MemoryCache.Key(this.key, this.extras)

@Serializable
data class ImageCacheKey(val key: String, val extras: Map<String, String>)

@Serializable
data class ImageState(
    val uri: String?,
    val cacheKey: ImageCacheKey? = null,
    val containerColorArgb: Int? = null,
    val textColorArgb: Int? = null,
)

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
    heightStartThreshold: Float = 0f,
    widthEndThreshold: Float = 1f,
    selectMaxPopulation: Boolean = false,
    colors: ImageColors? = null,
    requestColors: Boolean = false,
): CoilImageState {
    val imageColorsState = LocalImageColorsState.current
    return remember(uri, imageColorsState) {
        CoilImageState(
            uri = uri,
            imageCacheKey = cacheKey,
            heightStartThreshold = heightStartThreshold,
            widthEndThreshold = widthEndThreshold,
            selectMaxPopulation = selectMaxPopulation,
            imageColorsState = imageColorsState,
            cachedColors = colors ?: imageColorsState.getColorsNonComposable(uri)
                .takeIf { it.containerColor.isSpecified && it.textColor.isSpecified },
            requestColors = requestColors,
        )
    }
}

@Composable
fun rememberCoilImageState(imageState: ImageState?): CoilImageState {
    val containerColorArgb = imageState?.containerColorArgb
    val textColorArgb = imageState?.textColorArgb
    val colors = containerColorArgb?.let {
        textColorArgb?.let {
            ImageColors(
                containerColor = Color(containerColorArgb),
                textColor = Color(textColorArgb)
            )
        }
    }
    return rememberCoilImageState(
        uri = imageState?.uri,
        cacheKey = imageState?.cacheKey,
        colors = colors,
    )
}

class CoilImageState internal constructor(
    val uri: String?,
    var imageCacheKey: ImageCacheKey? = null,
    private val heightStartThreshold: Float = 0f,
    private val widthEndThreshold: Float = 1f,
    private val selectMaxPopulation: Boolean = false,
    private val imageColorsState: ImageColorsState,
    internal val cachedColors: ImageColors?,
    var requestColors: Boolean,
) {
    var success by mutableStateOf(false)
        internal set

    internal var readColors = requestColors
    private var deferredSuccess: AsyncImagePainter.State.Success? = null

    val colors: ImageColors
        @Composable
        get() {
            if (cachedColors != null) return cachedColors
            readColors = true
            deferredSuccess?.let(::calculateColors)
            deferredSuccess = null
            return imageColorsState.getColors(uri)
        }

    fun toImageState(colors: ImageColors? = null) = ImageState(
        uri = uri,
        cacheKey = imageCacheKey,
        containerColorArgb = colors?.containerColor?.toArgb(),
        textColorArgb = colors?.textColor?.toArgb(),
    )

    internal fun onSuccess(success: AsyncImagePainter.State.Success) {
        uri ?: return
        imageCacheKey = success.result.memoryCacheKey?.toImageCacheKey()
        this.success = true
        if (cachedColors == null) {
            if (readColors) {
                calculateColors(success)
            } else {
                deferredSuccess = success
            }
        }
    }

    private fun calculateColors(success: AsyncImagePainter.State.Success) {
        uri ?: return
        imageColorsState.calculatePalette(
            id = uri,
            image = success.result.image,
            heightStartThreshold = heightStartThreshold,
            widthEndThreshold = widthEndThreshold,
            selectMaxPopulation = selectMaxPopulation,
        )
    }
}

expect fun ImageRequest.Builder.allowHardware(allowHardware: Boolean): ImageRequest.Builder

@Composable
fun CoilImageState?.request() = ImageRequest.Builder(LocalPlatformContext.current)
    .data(this?.uri)
    .allowHardware(this?.readColors != true || this.cachedColors != null)
    .placeholderMemoryCacheKey(
        this?.imageCacheKey?.toMemoryCacheKey() ?: this?.uri?.let(MemoryCache::Key)
    )

@Composable
fun CoilImageState?.colorsOrDefault() = this?.colors ?: ImageColors.DEFAULT

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
        state?.onSuccess(it)
        onSuccess?.invoke(it)
    },
    onError = onError,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
    clipToBounds = clipToBounds,
)

