@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.images

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_next_page
import artistalleydatabase.modules.alley.generated.resources.alley_previous_page
import artistalleydatabase.modules.alley.generated.resources.alley_show_catalog_grid_content_description
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.decode.BlackholeDecoder
import coil3.request.CachePolicy
import coil3.request.Disposable
import coil3.request.ImageRequest
import coil3.size.Dimension
import com.thekeeperofpie.artistalleydatabase.alley.ui.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.SmallImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.WrappedViewConfiguration
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import com.thekeeperofpie.artistalleydatabase.utils_compose.MultiZoomableState
import com.thekeeperofpie.artistalleydatabase.utils_compose.OnChangeEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.ZoomSlider
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.rememberMultiZoomableState
import com.thekeeperofpie.artistalleydatabase.utils_compose.scroll.VerticalScrollbar
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@Composable
fun rememberImagePagerState(images: List<ImageWithDimensions>, initialImageIndex: Int): PagerState {
    val pageCount = when {
        images.isEmpty() -> 0
        images.size == 1 -> 1
        else -> images.size + 1
    }
    val maxIndex = (pageCount - 1).coerceAtLeast(0)
    val initialPage = initialImageIndex.coerceAtMost(maxIndex)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount },
    )
    OnChangeEffect(images) {
        pagerState.requestScrollToPage(initialPage)
    }
    return pagerState
}

@OptIn(ExperimentalCoilApi::class)
private val blackholeFactory = BlackholeDecoder.Factory()

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ImagePager(
    images: List<ImageWithDimensions>,
    pagerState: PagerState,
    sharedElementId: Any,
    onClickPage: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    clipCorners: Boolean = true,
    forceMinHeight: Boolean = true,
    imageContentScale: ContentScale = ContentScale.FillWidth,
    multiZoomableState: MultiZoomableState = rememberMultiZoomableState(images.size),
    downsample: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {
        val existingViewConfiguration = LocalViewConfiguration.current
        val newViewConfiguration = remember(existingViewConfiguration) {
            WrappedViewConfiguration(
                viewConfiguration = existingViewConfiguration,
                overrideTouchSlop = existingViewConfiguration.touchSlop * 4,
            )
        }
        val userScrollEnabled by remember(images) {
            derivedStateOf {
                if (images.size <= 1) return@derivedStateOf false
                val zoomableState =
                    multiZoomableState[(pagerState.currentPage - 1).coerceAtLeast(0)]
                val zoomFraction = zoomableState.zoomFraction ?: return@derivedStateOf true
                zoomFraction < 0.05f
            }
        }

        var anySuccess by remember { mutableStateOf(false) }

        val targetSizeWidth = Dimension(LocalWindowInfo.current.containerSize.width / 2)
        CompositionLocalProvider(LocalViewConfiguration provides newViewConfiguration) {
            var minHeight by remember { mutableIntStateOf(0) }
            val density = LocalDensity.current
            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                userScrollEnabled = userScrollEnabled,
                modifier = Modifier
                    .conditionally(forceMinHeight) {
                        heightIn(min = density.run { minHeight.toDp() })
                            .onSizeChanged {
                                if (it.height > minHeight) {
                                    minHeight = it.height
                                }
                            }
                    }
                    .conditionally(clipCorners) {
                        clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    }
                    .clipToBounds()
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (it == 0 && images.size > 1) {
                        SmallImageGrid(
                            targetHeight = if (forceMinHeight) {
                                minHeight.coerceAtLeast(
                                    density.run { 320.dp.roundToPx() }
                                )
                            } else {
                                null
                            },
                            images = images,
                            onImageClick = { index, _ ->
                                scope.launch {
                                    pagerState.animateScrollToPage(index + 1)
                                }
                            }
                        )
                    } else {
                        Box(modifier = modifier.fillMaxSize()) {
                            val imageIndex = (it - 1).coerceAtLeast(0)
                            val zoomableState = multiZoomableState[imageIndex]
                            val image = images[imageIndex]
                            val width = image.width
                            val height = image.height
                            val isFillWidth = imageContentScale == ContentScale.FillWidth

                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(image)
                                    .run {
                                        if (downsample) {
                                            size(targetSizeWidth, Dimension.Undefined)
                                        } else {
                                            this
                                        }
                                    }
                                    .memoryCacheKey(image.coilImageModel.toString())
                                    .build(),
                                contentScale = imageContentScale,
                                onSuccess = { anySuccess = true },
                                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                                modifier = Modifier
                                    .zoomable(
                                        state = zoomableState,
                                        onClick = if (onClickPage == null) null else {
                                            {
                                                onClickPage(pagerState.settledPage)
                                            }
                                        },
                                        clipToBounds = false,
                                    )
                                    .sharedElement("image", image.coilImageModel)
                                    .conditionally(isFillWidth) {
                                        fillMaxWidth()
                                    }
                                    .conditionally(isFillWidth && width != null && height != null) {
                                        aspectRatio(width!! / height!!.toFloat())
                                    }
                                    .conditionally(!isFillWidth) {
                                        fillMaxHeight()
                                    }
                                    .conditionally(clipCorners && LocalSharedTransitionScope.current.isTransitionActive) {
                                        clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp
                                            )
                                        )
                                    }
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }

        ImagePagerActions(
            sharedElementId = sharedElementId,
            images = images,
            pagerState = pagerState,
            userScrollEnabled = { userScrollEnabled },
        )

        val context = LocalPlatformContext.current
        DisposableEffect(context, images, anySuccess) {
            val imageLoader = SingletonImageLoader.get(context)
            val disposables = mutableListOf<Disposable>()
            if (anySuccess) {
                images.forEach {
                    disposables += imageLoader.enqueue(
                        ImageRequest.Builder(context)
                            .data(it)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .decoderFactory(blackholeFactory)
                            .build()
                    )
                }
            }
            onDispose { disposables.forEach { it.dispose() } }
        }
    }
}

@Composable
private fun BoxScope.ImagePagerActions(
    sharedElementId: Any,
    images: List<ImageWithDimensions>,
    pagerState: PagerState,
    userScrollEnabled: () -> Boolean,
) {
    AnimatedVisibility(
        visible = userScrollEnabled(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .sharedElement("pagerIndicator", sharedElementId, zIndexInOverlay = 1f)
                .padding(8.dp)
        )
    }

    val scope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = pagerState.currentPage != 0 && userScrollEnabled(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.BottomEnd)
    ) {
        IconButton(
            onClick = {
                scope.launch {
                    pagerState.animateScrollToPage(0)
                }
            },
            modifier = Modifier.sharedElement("gridIcon", sharedElementId, zIndexInOverlay = 1f)
        ) {
            Icon(
                imageVector = Icons.Filled.GridOn,
                contentDescription = stringResource(
                    Res.string.alley_show_catalog_grid_content_description
                )
            )
        }
    }

    val previousPageInteractionSource = remember { MutableInteractionSource() }
    AnimatedVisibility(
        visible = pagerState.pageCount > 1 && pagerState.currentPage != 0,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.CenterStart)
    ) {
        IconButton(
            onClick = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
            },
            modifier = Modifier.sharedElement("previousPage", sharedElementId, zIndexInOverlay = 1f)
                .hoverable(previousPageInteractionSource)
        ) {
            val previousPageIsHovered by previousPageInteractionSource.collectIsHoveredAsState()
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceDim
                            .copy(alpha = if (previousPageIsHovered) 0.15f else 0.5f),
                        shape = CircleShape,
                    )
            ) {
                val willPageToGrid = images.size > 1 && pagerState.currentPage == 1
                Icon(
                    imageVector = if (willPageToGrid) {
                        Icons.Default.GridView
                    } else {
                        Icons.AutoMirrored.Filled.ArrowLeft
                    },
                    contentDescription = stringResource(Res.string.alley_previous_page),
                    modifier = Modifier.conditionally(willPageToGrid) { size(16.dp) }
                )
            }
        }
    }

    AnimatedVisibility(
        visible = pagerState.currentPage < pagerState.pageCount - 1,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.CenterEnd)
    ) {
        val nextPageInteractionSource = remember { MutableInteractionSource() }
        IconButton(
            onClick = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier.sharedElement("nextPage", sharedElementId, zIndexInOverlay = 1f)
                .hoverable(nextPageInteractionSource)
        ) {
            val nextPageIsHovered by nextPageInteractionSource.collectIsHoveredAsState()
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = stringResource(Res.string.alley_next_page),
                modifier = Modifier.padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceDim
                            .copy(alpha = if (nextPageIsHovered) 0.15f else 0.5f),
                        shape = CircleShape,
                    )
            )
        }
    }
}

@Composable
fun ImageGrid(
    images: List<ImageWithDimensions>,
    onClickImage: (imageIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Cache dimensions for images without them so that item height is preserved when scrolling
    val cachedDimensions = remember(images) {
        SnapshotStateMap<ImageWithDimensions, IntSize>().apply {
            this += images.mapNotNull {
                val width = it.width ?: return@mapNotNull null
                val height = it.height ?: return@mapNotNull null
                it to IntSize(width, height)
            }
        }
    }

    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant
    val placeholder = remember { ColorPainter(placeholderColor) }

    Row(modifier = modifier) {
        val gridState = rememberLazyStaggeredGridState()
        LazyVerticalStaggeredGrid(
            state = gridState,
            columns = StaggeredGridCells.Adaptive(500.dp),
            contentPadding = PaddingValues(8.dp),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(images) { index, image ->
                Box {
                    val zoomableState = rememberZoomableState(ZoomSpec(maxZoomFactor = 3f))
                    val size = cachedDimensions[image]
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(image)
                            .placeholderMemoryCacheKey(image.coilImageModel.toString())
                            .build(),
                        contentScale = ContentScale.FillWidth,
                        contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                        placeholder = placeholder,
                        onSuccess = {
                            val width = it.result.image.width
                            val height = it.result.image.height
                            if (width > 0 && height > 0) {
                                cachedDimensions[image] = IntSize(width, height)
                            }
                        },
                        modifier = Modifier
                            .zoomable(
                                state = zoomableState,
                                onClick = { onClickImage(index) },
                            )
                            .sharedElement("image", image.coilImageModel)
                            .fillMaxWidth()
                            .conditionallyNonNull(size) {
                                aspectRatio(it.width.toFloat() / it.height)
                            }
                    )

                    val isZoomed = zoomableState.zoomFraction.let { it != null && it > 0f }
                    val sliderVisible = isZoomed && zoomableState.isAnimationRunning
                    val alpha by animateFloatAsState(
                        if (sliderVisible) 1f else 0f,
                        if (sliderVisible) {
                            tween(durationMillis = 100)
                        } else {
                            tween(durationMillis = 2.seconds.inWholeMilliseconds.toInt())
                        },
                    )
                    ZoomSlider(
                        state = zoomableState,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .widthIn(max = 480.dp)
                            .align(Alignment.BottomCenter)
                            .graphicsLayer { this.alpha = alpha }
                    )
                }
            }
        }

        VerticalScrollbar(
            state = gridState,
            alwaysVisible = true,
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 8.dp, bottom = 72.dp)
        )
    }
}
