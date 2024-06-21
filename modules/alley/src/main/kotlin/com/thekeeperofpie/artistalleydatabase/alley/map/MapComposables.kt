@file:OptIn(ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.alley.map

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.thekeeperofpie.artistalleydatabase.alley.R
import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryGridModel
import com.thekeeperofpie.artistalleydatabase.compose.conditionally

@Composable
fun TableCell(
    table: Table,
    background: Color = if (table.image != null) {
        MaterialTheme.colorScheme.primary
    } else {
        table.section.color.copy(alpha = 0.25f).compositeOver(MaterialTheme.colorScheme.surface)
    },
    borderWidth: Dp = 1.dp,
    borderColor: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = if (table.image != null) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        table.section.textColor.copy(alpha = 0.33f)
            .compositeOver(MaterialTheme.colorScheme.onSurface)
    },
    onArtistClick: (ArtistEntryGridModel, Int) -> Unit,
) {
    var showPopup by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
            .background(background)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RectangleShape,
            )
            .clickable { showPopup = true }
    ) {
        val imageUri = table.image?.uri
        if (imageUri != null) {
            BoxWithConstraints {
                val minSize = LocalDensity.current.run { 80.dp }
                if (maxWidth > minSize && maxHeight > minSize) {
                    AsyncImage(
                        model = imageUri,
                        contentScale = ContentScale.FillWidth,
                        fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                        contentDescription = stringResource(R.string.alley_artist_catalog_image),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        val viewModel = hiltViewModel<MapViewModel>()
        if (showPopup) {
            BackHandler { showPopup = false }
            var tableEntry by remember { mutableStateOf<ArtistEntryGridModel?>(null) }
            LaunchedEffect(table) {
                tableEntry = viewModel.tableEntry(table)
            }
            Popup(
                alignment = Alignment.TopCenter,
                onDismissRequest = { showPopup = false },
            ) {
                val entry = tableEntry
                if (entry == null) {
                    CircularProgressIndicator()
                } else {
                    TablePopup(
                        entry = entry,
                        onFavoriteToggle = {
                            entry.favorite = it
                            viewModel.onFavoriteToggle(entry, it)
                        },
                        onIgnoredToggle = {
                            entry.ignored = it
                            viewModel.onIgnoredToggle(entry, it)
                        },
                        onClick = onArtistClick,
                        modifier = Modifier.sizeIn(maxWidth = 300.dp)
                    )
                }
            }
        }
        Text(
            text = table.booth,
            color = textColor,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun TablePopup(
    entry: ArtistEntryGridModel?,
    onFavoriteToggle: (Boolean) -> Unit,
    onIgnoredToggle: (Boolean) -> Unit,
    onClick: (ArtistEntryGridModel, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ignored = entry?.ignored ?: false
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { entry?.images?.size ?: 0 },
    )
    OutlinedCard(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { entry?.let { onClick(it, pagerState.settledPage) } },
                onLongClick = { onIgnoredToggle(!ignored) }
            )
            .alpha(if (entry?.ignored == true) 0.38f else 1f)
    ) {
        if (entry == null) {
            CircularProgressIndicator()
            return@OutlinedCard
        }

        val images = entry.images

        if (images.isNotEmpty()) {
            var minHeight by remember { mutableIntStateOf(0) }
            val coroutineScope = rememberCoroutineScope()
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val density = LocalDensity.current
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 16.dp,
                    modifier = Modifier
                        .heightIn(min = density.run { minHeight.toDp() })
                        .onSizeChanged {
                            if (it.height > minHeight) {
                                minHeight = it.height
                            }
                        }
                ) {
                    BoxWithConstraints {
                        val image = images[it]
                        AsyncImage(
                            model = image.uri,
                            contentScale = ContentScale.FillWidth,
                            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                            contentDescription = stringResource(R.string.alley_artist_catalog_image),
                            modifier = Modifier
                                .clickable { onClick(entry, pagerState.settledPage) }
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .conditionally(image.width != null && image.height != null) {
                                    height((image.height!! / image.width!!.toFloat()) * maxWidth)
                                }
                        )
                    }
                }

                if (images.size > 1) {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        pageCount = pagerState.pageCount,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(start = 16.dp)
                .conditionally(images.size > 1) { fillMaxWidth() }
        ) {
            Text(
                text = entry.value.booth,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = entry.value.name,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            )

            val favorite = entry.favorite
            IconButton(onClick = { onFavoriteToggle(!favorite) }) {
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
}
