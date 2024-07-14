@file:OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.alley.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.ImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen.SearchEntryModel
import com.thekeeperofpie.artistalleydatabase.compose.ZoomPanBox
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.rememberZoomPanState
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.LocalSharedTransitionScope
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.renderInSharedTransitionScopeOverlay
import com.thekeeperofpie.artistalleydatabase.compose.sharedtransition.sharedElement
import kotlinx.coroutines.launch
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
                Text(
                    text = entry.booth,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                        .sharedElement("booth", sharedElementId, zIndexInOverlay = 1f)
                )

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
                            R.string.alley_favorite_icon_content_description
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

@Composable
private fun ImagePager(
    images: List<CatalogImage>,
    pagerState: PagerState,
    sharedElementId: Any,
    onClickPage: (Int) -> Unit,
    clipCorners: Boolean = true,
) {
    val zoomPanState = rememberZoomPanState()
    val coroutineScope = rememberCoroutineScope()
    Box {
        var minHeight by remember { mutableIntStateOf(0) }
        val density = LocalDensity.current
        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            userScrollEnabled = images.size > 1 && zoomPanState.canPanExternal(),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .heightIn(min = density.run { minHeight.toDp() })
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
            if (it == 0 && images.size > 1) {
                ImageGrid(
                    targetHeight = minHeight.coerceAtLeast(
                        density.run { 320.dp.roundToPx() }
                    ),
                    images = images,
                    onImageClick = { index, _ ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index + 1)
                        }
                    }
                )
            } else {
                ZoomPanBox(state = zoomPanState) {
                    val image = images[(it - 1).coerceAtLeast(0)]
                    val width = image.width
                    val height = image.height
                    AsyncImage(
                        model = image.uri,
                        contentScale = ContentScale.FillWidth,
                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                        contentDescription = stringResource(R.string.alley_artist_catalog_image),
                        modifier = Modifier
                            .pointerInput(zoomPanState) {
                                detectTapGestures(
                                    onTap = { onClickPage(pagerState.settledPage) },
                                    onDoubleTap = {
                                        coroutineScope.launch {
                                            zoomPanState.toggleZoom(it, size)
                                        }
                                    }
                                )
                            }
                            // This breaks page scrolling
//                        .sharedElement("image", sharedElementId)
                            .fillMaxWidth()
                            .conditionally(width != null && height != null) {
                                aspectRatio(width!! / height!!.toFloat())
                            }
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .conditionally(clipCorners && LocalSharedTransitionScope.current.isTransitionActive) {
                                clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            }
                    )
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = images.size > 1 && zoomPanState.canPanExternal(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                pageCount = pagerState.pageCount,
                modifier = Modifier
                    .sharedElement("pagerIndicator", sharedElementId, zIndexInOverlay = 1f)
                    .padding(8.dp)
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = pagerState.currentPage != 0 && zoomPanState.canPanExternal(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                modifier = Modifier.sharedElement("gridIcon", sharedElementId)
            ) {
                Icon(
                    imageVector = Icons.Filled.GridOn,
                    contentDescription = stringResource(
                        R.string.alley_show_catalog_grid_content_description
                    )
                )
            }
        }
    }
}
