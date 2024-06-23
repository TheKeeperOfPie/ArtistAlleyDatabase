@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.alley.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.ImageGrid
import com.thekeeperofpie.artistalleydatabase.alley.LocalStableRandomSeed
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.SearchScreen.SearchEntryModel
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.ZoomPanBox
import com.thekeeperofpie.artistalleydatabase.compose.ZoomPanState
import com.thekeeperofpie.artistalleydatabase.compose.rememberZoomPanState
import com.thekeeperofpie.artistalleydatabase.compose.sharedBounds
import com.thekeeperofpie.artistalleydatabase.compose.sharedElement
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
    val pageCount = if (images.isEmpty()) {
        0
    } else if (images.size == 1) {
        1
    } else {
        images.size + 1
    }
    val pagerState = rememberPagerState(
        initialPage = if (showGridByDefault || images.isEmpty()) {
            0
        } else if (showRandomCatalogImage) {
            (1..images.size).random(Random(LocalStableRandomSeed.current + entry.id.hashCode()))
        } else {
            1
        },
        pageCount = { pageCount },
    )

    val ignored = entry.ignored
    ElevatedCard(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .combinedClickable(
                onClick = { onClick(entry, pagerState.settledPage) },
                onLongClick = { onIgnoredToggle(!ignored) }
            )
            .alpha(if (entry.ignored) 0.38f else 1f)
    ) {
        if (images.isNotEmpty()) {
            val zoomPanState = rememberZoomPanState()
            val coroutineScope = rememberCoroutineScope()
            Box {
                ImagePager(
                    images = images,
                    pagerState = pagerState,
                    sharedElementId = sharedElementId,
                    zoomPanState = zoomPanState,
                    onClickPage = { onClick(entry, it) },
                )

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
                            .sharedBounds("pagerIndicator", sharedElementId, zIndexInOverlay = 1f)
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
                        modifier = Modifier.sharedBounds("gridIcon", sharedElementId)
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

        itemRow(entry, onFavoriteToggle, Modifier)
    }
}

@Composable
private fun ImagePager(
    images: List<CatalogImage>,
    pagerState: PagerState,
    sharedElementId: Any,
    zoomPanState: ZoomPanState,
    onClickPage: (Int) -> Unit,
) {
    var minHeight by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    HorizontalPager(
        state = pagerState,
        pageSpacing = 16.dp,
        userScrollEnabled = images.size > 1 && zoomPanState.canPanExternal(),
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .sharedBounds("imageContainer", sharedElementId)
            .heightIn(min = density.run { minHeight.toDp() })
            .onSizeChanged {
                if (it.height > minHeight) {
                    minHeight = it.height
                }
            }
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
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
                val cornerRadius by remember {
                    derivedStateOf {
                        if (pagerState.isScrollInProgress) 0.dp else 12.dp
                    }
                }
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
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
                )
            }
        }
    }
}

@Composable
fun ArtistListRow(
    entry: ArtistEntryGridModel,
    onFavoriteToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val artist = entry.value
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .sharedBounds("container", artist.id, zIndexInOverlay = 1f)
            .fillMaxWidth()
    ) {
        Text(
            text = artist.booth,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .sharedBounds("booth", artist.id, zIndexInOverlay = 1f)
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
        )

        Text(
            text = artist.name,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .sharedBounds("name", artist.id, zIndexInOverlay = 1f)
                .weight(1f)
                .padding(vertical = 12.dp)
        )

        val favorite = entry.favorite
        IconButton(
            onClick = { onFavoriteToggle(!favorite) },
            modifier = Modifier.sharedElement(
                "favorite",
                artist.id,
                zIndexInOverlay = 1f,
            )
        ) {
            Icon(
                imageVector = if (favorite) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Filled.FavoriteBorder
                },
                contentDescription = stringResource(
                    R.string.alley_artist_favorite_icon_content_description
                ),
            )
        }
    }
}
