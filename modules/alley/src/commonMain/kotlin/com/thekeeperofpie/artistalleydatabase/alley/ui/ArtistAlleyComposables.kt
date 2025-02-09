@file:OptIn(
    ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3Api::class
)

package com.thekeeperofpie.artistalleydatabase.alley.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_favorite_icon_content_description
import artistalleydatabase.modules.alley.generated.resources.alley_next_page
import artistalleydatabase.modules.alley.generated.resources.alley_previous_page
import artistalleydatabase.modules.alley.generated.resources.alley_show_catalog_grid_content_description
import coil3.compose.AsyncImage
import com.eygraber.uri.Uri
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen.SearchEntryModel
import com.thekeeperofpie.artistalleydatabase.alley.data.CatalogImage
import com.thekeeperofpie.artistalleydatabase.utils_compose.LocalWindowConfiguration
import com.thekeeperofpie.artistalleydatabase.utils_compose.ZoomPanBox
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalAnimatedVisibilityScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.renderInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionallyNonNull
import com.thekeeperofpie.artistalleydatabase.utils_compose.rememberZoomPanState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@Composable
fun <EntryModel : SearchEntryModel> ItemCard(
    entry: EntryModel,
    sharedElementId: Any,
    showGridByDefault: Boolean,
    showRandomCatalogImage: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onIgnoredToggle: (Boolean) -> Unit,
    onClick: (EntryModel, Int) -> Unit,
    modifier: Modifier = Modifier,
    itemRow: @Composable (
        entry: EntryModel,
        onFavoriteToggle: (Boolean) -> Unit,
        modifier: Modifier,
    ) -> Unit,
) {
    val images = entry.images
    val pagerState = rememberPagerState(
        entry = entry,
        images = images,
        showGridByDefault = showGridByDefault,
        showRandomCatalogImage = showRandomCatalogImage,
    )

    val ignored = entry.ignored
    ElevatedCard(
        modifier = modifier
            .combinedClickable(
                onClick = { onClick(entry, pagerState.settledPage) },
                onLongClick = { onIgnoredToggle(!ignored) }
            )
            .alpha(if (entry.ignored) 0.38f else 1f)
    ) {
        if (images.isNotEmpty()) {
            ImagePager(
                images = images,
                pagerState = pagerState,
                sharedElementId = sharedElementId,
                onClickPage = { onClick(entry, it) },
            )
        }

        itemRow(entry, onFavoriteToggle, Modifier)
    }
}

@Composable
fun <EntryModel : SearchEntryModel> ItemImage(
    entry: EntryModel,
    sharedElementId: Any,
    showGridByDefault: Boolean,
    showRandomCatalogImage: Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onIgnoredToggle: (Boolean) -> Unit,
    onClick: (EntryModel, Int) -> Unit,
    modifier: Modifier = Modifier,
    itemRow: @Composable (
        entry: EntryModel,
        onFavoriteToggle: (Boolean) -> Unit,
        modifier: Modifier,
    ) -> Unit,
) {
    val images = entry.images
    val pagerState = rememberPagerState(
        entry = entry,
        images = images,
        showGridByDefault = showGridByDefault,
        showRandomCatalogImage = showRandomCatalogImage,
    )

    val ignored = entry.ignored
    Box(
        modifier = modifier
            .combinedClickable(
                onClick = { onClick(entry, pagerState.settledPage) },
                onLongClick = { onIgnoredToggle(!ignored) }
            )
            .background(color = MaterialTheme.colorScheme.surface)
            .run {
                if (images.isEmpty()) {
                    border(width = Dp.Hairline, color = MaterialTheme.colorScheme.surfaceBright)
                } else {
                    border(width = Dp.Hairline, color = MaterialTheme.colorScheme.surfaceDim)
                }
            }
            .alpha(if (entry.ignored) 0.38f else 1f)
    ) {
        if (images.isEmpty()) {
            itemRow(entry, onFavoriteToggle, Modifier)
        } else {
            ImagePager(
                images = images,
                pagerState = pagerState,
                sharedElementId = sharedElementId,
                onClickPage = { onClick(entry, it) },
                clipCorners = false,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(topEnd = 12.dp)
                    )
                    .renderInSharedTransitionScopeOverlay()
            ) {
                val booth = entry.booth
                if (booth != null) {
                    Text(
                        text = booth,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                            .sharedElement("booth", sharedElementId, zIndexInOverlay = 1f)
                    )
                }

                IconButton(
                    onClick = { onFavoriteToggle(!entry.favorite) },
                    modifier = Modifier
                        .sharedElement("favorite", sharedElementId, zIndexInOverlay = 1f)
                ) {
                    Icon(
                        imageVector = if (entry.favorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Filled.FavoriteBorder
                        },
                        contentDescription = stringResource(
                            Res.string.alley_favorite_icon_content_description
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun <EntryModel : SearchEntryModel> rememberPagerState(
    entry: EntryModel,
    images: List<CatalogImage>,
    showGridByDefault: Boolean,
    showRandomCatalogImage: Boolean,
): PagerState {
    val pageCount = if (images.isEmpty()) {
        0
    } else if (images.size == 1) {
        1
    } else {
        images.size + 1
    }
    return rememberPagerState(
        initialPage = if (showGridByDefault || images.isEmpty()) {
            0
        } else if (showRandomCatalogImage) {
            (1..images.size).random(Random(LocalStableRandomSeed.current + entry.id.hashCode()))
        } else {
            1
        },
        pageCount = { pageCount },
    )
}

private class WrappedViewConfiguration(
    viewConfiguration: ViewConfiguration,
    val overrideTouchSlop: Float,
) : ViewConfiguration by viewConfiguration {
    override val touchSlop = overrideTouchSlop
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
    imageContentScale: ContentScale = ContentScale.FillWidth,
) {
    val zoomPanState = rememberZoomPanState()
    val scope = rememberCoroutineScope()
    Box(Modifier.conditionallyNonNull(onClickOutside) { clickable(onClick = it) }) {
        val existingViewConfiguration = LocalViewConfiguration.current
        val newViewConfiguration = remember(existingViewConfiguration) {
            WrappedViewConfiguration(
                viewConfiguration = existingViewConfiguration,
                overrideTouchSlop = existingViewConfiguration.touchSlop * 2,
            )
        }
        CompositionLocalProvider(LocalViewConfiguration provides newViewConfiguration) {
            var minHeight by remember { mutableIntStateOf(0) }
            val density = LocalDensity.current
            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                userScrollEnabled = images.size > 1 && zoomPanState.canPanExternal(),
                modifier = Modifier
                    .heightIn(min = density.run { minHeight.toDp() })
                    .then(modifier)
                    .onSizeChanged {
                        if (it.height > minHeight) {
                            minHeight = it.height
                        }
                    }
                    .conditionally(clipCorners) {
                        clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    }
                    .sharedElement("imageContainer", sharedElementId)
                    .clipToBounds()
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (it == 0 && images.size > 1) {
                        SmallImageGrid(
                            targetHeight = minHeight.coerceAtLeast(
                                density.run { 320.dp.roundToPx() }
                            ),
                            images = images,
                            onImageClick = { index, _ ->
                                scope.launch {
                                    pagerState.animateScrollToPage(index + 1)
                                }
                            }
                        )
                    } else {
                        ZoomPanBox(state = zoomPanState, onClick = onClickOutside) {
                            val image = images[(it - 1).coerceAtLeast(0)]
                            val width = image.width
                            val height = image.height
                            val isFillWidth = imageContentScale == ContentScale.FillWidth
                            AsyncImage(
                                model = image.uri,
                                contentScale = imageContentScale,
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
                                        clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                    }
                                    .align(Alignment.Center)
                                // Causes page buttons to render underneath
                                // .sharedElement("image", image.uri)
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = images.size > 1 && zoomPanState.canPanExternal(),
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

        AnimatedVisibility(
            visible = pagerState.currentPage != 0 && zoomPanState.canPanExternal(),
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
                modifier = Modifier.sharedElement("gridIcon", sharedElementId)
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
                modifier = Modifier.sharedElement("previousPage", sharedElementId)
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
                modifier = Modifier.sharedElement("nextPage", sharedElementId)
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
}

@Composable
fun Modifier.sharedElement(
    vararg keys: Any?,
    zIndexInOverlay: Float = 0f,
    animatedVisibilityScope: AnimatedVisibilityScope = LocalAnimatedVisibilityScope.current,
): Modifier {
    if (keys.contains(null)) return this
    if (keys.any { it is SharedTransitionKey && (it.key == "null" || it.key.isEmpty()) }) return this
    return with(LocalSharedTransitionScope.current) {
        sharedElement(
            rememberSharedContentState(key = keys.toList()),
            animatedVisibilityScope = animatedVisibilityScope,
            zIndexInOverlay = zIndexInOverlay,
        )
    }
}

@Composable
fun Modifier.sharedBounds(vararg keys: Any?, zIndexInOverlay: Float = 0f): Modifier {
    if (keys.contains(null)) return this
    if (keys.any { it is SharedTransitionKey && (it.key == "null" || it.key.isEmpty()) }) return this
    // TODO: sharedBounds causes bugs with scrolling?
    return with(LocalSharedTransitionScope.current) {
        sharedBounds(
            rememberSharedContentState(key = keys.toList()),
            animatedVisibilityScope = LocalAnimatedVisibilityScope.current,
            zIndexInOverlay = zIndexInOverlay,
        )
    }
}

@Composable
fun HorizontalPagerIndicator(pagerState: PagerState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        repeat(pagerState.pageCount) {
            val color = if (pagerState.currentPage == it) Color.DarkGray else Color.LightGray
            Box(
                modifier = Modifier
                    .clickable { scope.launch { pagerState.animateScrollToPage(it) } }
                    .padding(2.dp)
                    .background(color, CircleShape)
                    .border(1.dp, Color.DarkGray, CircleShape)
                    .size(8.dp)
            )
        }
    }
}

@Composable
internal fun SmallImageGrid(
    targetHeight: Int? = null,
    images: List<CatalogImage>,
    onImageClick: (index: Int, image: Uri) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .let {
                if (targetHeight == null) {
                    it
                } else if (targetHeight > 0) {
                    it.height(density.run { targetHeight.toDp() })
                } else {
                    it.heightIn(max = 320.dp)
                }
            }
    ) {
        itemsIndexed(images) { index, image ->
            AsyncImage(
                model = image.uri,
                contentScale = ContentScale.FillWidth,
                contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                modifier = Modifier
                    .clickable { onImageClick(index, image.uri) }
                    .sharedElement("gridImage", image.uri)
                    .fillMaxWidth()
                    .conditionally(image.width != null && image.height != null) {
                        aspectRatio(image.height!!.toFloat() / image.width!!)
                    }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun currentWindowSizeClass(): WindowSizeClass {
    val density = LocalDensity.current
    val windowConfiguration = LocalWindowConfiguration.current
    val width = windowConfiguration.screenWidthDp
    val height = windowConfiguration.screenHeightDp
    return remember(density, windowConfiguration) {
        WindowSizeClass.calculateFromSize(DpSize(width, height))
    }
}

// TooltipBox crashes on web
@Composable
fun Tooltip(
    text: String,
    popupAlignment: Alignment = Alignment.BottomCenter,
    content: @Composable () -> Unit,
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val contentInteractionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .hoverable(contentInteractionSource)
            .onGloballyPositioned { size = it.size }
    ) {
        content()

        val popupInteractionSource = remember { MutableInteractionSource() }
        val contentIsHovered by contentInteractionSource.collectIsHoveredAsState()
        val popupIsHovered by popupInteractionSource.collectIsHoveredAsState()
        if (contentIsHovered || popupIsHovered) {
            Popup(
                alignment = popupAlignment,
                offset = IntOffset(0, -size.height),
            ) {
                Text(
                    text = text,
                    modifier = Modifier
                        .hoverable(popupInteractionSource)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceDim,
                            shape = MaterialTheme.shapes.small,
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(8.dp)
                        .widthIn(max = 240.dp)
                )
            }
        }
    }
}

@Composable
fun IconWithTooltip(
    imageVector: ImageVector,
    tooltipText: String,
    onClick: () -> Unit,
    contentDescription: String? = null,
) {
    Tooltip(text = tooltipText) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.height(20.dp)
            )
        }
    }
}

