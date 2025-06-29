@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.images

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_next_page
import artistalleydatabase.modules.alley.generated.resources.alley_previous_page
import artistalleydatabase.modules.alley.generated.resources.alley_show_catalog_grid_content_description
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.Disposable
import coil3.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.ui.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.alley.ui.SmallImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.ui.WrappedViewConfiguration
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.MultiZoomPanState
import com.thekeeperofpie.artistalleydatabase.utils_compose.OnChangeEffect
import com.thekeeperofpie.artistalleydatabase.utils_compose.ZoomPanBox
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberImagePagerState(images: List<CatalogImage>, initialImageIndex: Int): PagerState {
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

@Composable
fun ImagePager(
    images: List<CatalogImage>,
    pagerState: PagerState,
    sharedElementId: Any,
    onClickPage: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    onClickOutside: (() -> Unit)? = null,
    clipCorners: Boolean = true,
    forceMinHeight: Boolean = true,
    imageContentScale: ContentScale = ContentScale.FillWidth,
    zoomPanStates: MultiZoomPanState = rememberSaveable(
        images,
        LocalDensity.current,
        saver = MultiZoomPanState.Saver
    ) { MultiZoomPanState(images.size) },
) {
    val scope = rememberCoroutineScope()
    Box(modifier.conditionallyNonNull(onClickOutside) { clickable(onClick = it) }) {
        val existingViewConfiguration = LocalViewConfiguration.current
        val newViewConfiguration = remember(existingViewConfiguration) {
            WrappedViewConfiguration(
                viewConfiguration = existingViewConfiguration,
                overrideTouchSlop = existingViewConfiguration.touchSlop * 4,
            )
        }
        val userScrollEnabled by remember(images) {
            derivedStateOf {
                images.size > 1 && zoomPanStates[(pagerState.currentPage - 1).coerceAtLeast(0)]
                    .canPanExternal()
            }
        }

        var anySuccess by remember { mutableStateOf(false) }

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
                        val imageIndex = (it - 1).coerceAtLeast(0)
                        val zoomPanState = zoomPanStates[imageIndex]
                        ZoomPanBox(state = zoomPanState, onClick = onClickOutside) {
                            val image = images[imageIndex]
                            val width = image.width
                            val height = image.height
                            val isFillWidth = imageContentScale == ContentScale.FillWidth
                            AsyncImage(
                                model = ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(image.uri)
                                    .memoryCacheKey(image.uri.toString())
                                    .build(),
                                contentScale = imageContentScale,
                                onSuccess = { anySuccess = true },
                                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                                contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                                modifier = Modifier
                                    .pointerInput(zoomPanState) {
                                        detectTapGestures(
                                            onTap = if (onClickPage == null) {
                                                null
                                            } else {
                                                { onClickPage(pagerState.settledPage) }
                                            },
                                            onDoubleTap = {
                                                scope.launch {
                                                    zoomPanState.toggleZoom(it, size)
                                                }
                                            }
                                        )
                                    }
                                    .sharedElement("image", image.uri)
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
                            .data(it.uri)
                            .memoryCacheKey(it.uri.toString())
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
    images: List<CatalogImage>,
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
