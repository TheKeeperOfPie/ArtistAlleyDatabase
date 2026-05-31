package com.thekeeperofpie.artistalleydatabase.alley.changelog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import artistalleydatabase.modules.alley.generated.resources.Res
import artistalleydatabase.modules.alley.generated.resources.alley_artist_catalog_image
import artistalleydatabase.modules.alley.generated.resources.alley_next_page
import artistalleydatabase.modules.alley.generated.resources.alley_previous_page
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.thekeeperofpie.artistalleydatabase.alley.images.CatalogImage
import com.thekeeperofpie.artistalleydatabase.alley.ui.rememberSharedContentState
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.icons.Icons
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.ArrowLeft
import com.thekeeperofpie.artistalleydatabase.icons.automirrored.filled.ArrowRight
import com.thekeeperofpie.artistalleydatabase.icons.filled.ImageNotSupported
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChangelogImages(
    sharedElementId: Any,
    images: List<CatalogImage>,
    onClickImage: (CatalogImage) -> Unit,
) {
    Box {
        val listState = rememberLazyListState()
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(items = images, key = { it.coilImageModel.toString() }) {
                val sharedContentState =
                    rememberSharedContentState("image", it.coilImageModel)
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(it.coilImageModel)
                        .placeholderMemoryCacheKey(it.coilImageModel.toString())
                        .build(),
                    contentScale = ContentScale.Fit,
                    fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                    contentDescription = stringResource(Res.string.alley_artist_catalog_image),
                    modifier = Modifier
                        .height(ChangelogUtils.ImageHeight)
                        .widthIn(min = 48.dp)
                        .sharedElement(state = sharedContentState)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClickImage(it) }
                )
            }
        }

        val scrollSize = with(LocalDensity.current) { ChangelogUtils.ImageHeight.toPx() }
        val scope = rememberCoroutineScope()

        val previousPageInteractionSource = remember { MutableInteractionSource() }
        IconButton(
            onClick = {
                scope.launch {
                    if (listState.firstVisibleItemScrollOffset > scrollSize / 10) {
                        listState.animateScrollToItem(listState.firstVisibleItemIndex)
                    } else {
                        val target = listState.firstVisibleItemIndex - 1
                        if (target >= 0) {
                            listState.animateScrollToItem(target)
                        }
                        if (listState.firstVisibleItemIndex == target + 1) {
                            listState.animateScrollBy(-scrollSize)
                        }
                    }
                }
            },
            modifier = Modifier
                .sharedElement("previousPage", sharedElementId, zIndexInOverlay = 1f)
                .align(Alignment.CenterStart)
                .hoverable(previousPageInteractionSource)
                .visible(listState.canScrollBackward)
        ) {
            val previousPageIsHovered by previousPageInteractionSource.collectIsHoveredAsState()
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowLeft,
                contentDescription = stringResource(Res.string.alley_previous_page),
                modifier = Modifier.padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceDim
                            .copy(alpha = if (previousPageIsHovered) 0.15f else 0.5f),
                        shape = CircleShape,
                    )
            )
        }

        val nextPageInteractionSource = remember { MutableInteractionSource() }
        IconButton(
            onClick = {
                scope.launch {
                    val target = listState.layoutInfo.visibleItemsInfo.lastIndex + 1
                    if (target < listState.layoutInfo.totalItemsCount) {
                        listState.animateScrollToItem(target)
                    }
                    if (listState.layoutInfo.visibleItemsInfo.lastIndex == target - 1) {
                        listState.animateScrollBy(scrollSize)
                    }
                }
            },
            modifier = Modifier
                .sharedElement("nextPage", sharedElementId, zIndexInOverlay = 1f)
                .align(Alignment.CenterEnd)
                .hoverable(nextPageInteractionSource)
                .visible(listState.canScrollForward)
        ) {
            val nextPageIsHovered by nextPageInteractionSource.collectIsHoveredAsState()
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowRight,
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
