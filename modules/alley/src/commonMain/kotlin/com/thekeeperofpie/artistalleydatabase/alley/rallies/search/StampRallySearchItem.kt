package com.thekeeperofpie.artistalleydatabase.alley.rallies.search

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.thekeeperofpie.artistalleydatabase.alley.images.ImagePager
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyWithUserData
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyWithUserDataProvider
import com.thekeeperofpie.artistalleydatabase.alley.rallies.ui.StampRallyListRow
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchDisplayType
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchItemCard
import com.thekeeperofpie.artistalleydatabase.alley.search.SearchItemImage
import com.thekeeperofpie.artistalleydatabase.alley.search.rememberSearchPagerState
import com.thekeeperofpie.artistalleydatabase.alley.series.SeriesImageInfo
import com.thekeeperofpie.artistalleydatabase.alley.ui.FavoriteIconButton
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedBounds
import com.thekeeperofpie.artistalleydatabase.alley.ui.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils.ImageWithDimensions
import com.thekeeperofpie.artistalleydatabase.utils_compose.border
import com.thekeeperofpie.artistalleydatabase.utils_preview.AlleyPreview

@Composable
fun StampRallySearchItem(
    displayType: SearchDisplayType,
    stampRallyWithUserData: StampRallyWithUserData,
    showGridByDefault: Boolean,
    showRandomCatalogImage: Boolean,
    blockCrossAxisScrolling: () -> Boolean,
    onFavoriteToggle: (Boolean) -> Unit,
    onIgnoredToggle: (Boolean) -> Unit,
    onClick: (imageIndex: Int) -> Unit,
    onClickFullscreen: (imageIndex: Int) -> Unit,
    seriesImage: (SeriesImageInfo) -> String?,
) {
    val stampRally = stampRallyWithUserData.stampRally
    val userEntry = stampRallyWithUserData.userEntry
    val sharedElementId = stampRally.id
    val images = stampRallyWithUserData.images
    val pagerState = rememberSearchPagerState(
        entryId = stampRally.id,
        images = images,
        showGridByDefault = showGridByDefault,
        showRandomCatalogImage = showRandomCatalogImage,
    )
    val ignored = userEntry.ignored
    when (displayType) {
        SearchDisplayType.LIST -> {
            StampRallyListRow(
                stampRallyWithUserData = stampRallyWithUserData,
                onFavoriteToggle = onFavoriteToggle,
                seriesImage = seriesImage,
                modifier = Modifier
                    .sharedBounds("itemContainer", sharedElementId)
                    .combinedClickable(
                        onClick = { onClick(1) },
                        onLongClick = { onIgnoredToggle(!ignored) }
                    )
                    .alpha(if (ignored) 0.38f else 1f)
                    .border(
                        width = 1.dp,
                        color = DividerDefaults.color,
                        start = true,
                        bottom = true,
                    )
            )
        }
        SearchDisplayType.CARD ->
            SearchItemCard(
                ignored = ignored,
                onIgnoredToggle = onIgnoredToggle,
                onClick = { onClick(pagerState.settledPage) },
                modifier = Modifier.sharedBounds("itemContainer", sharedElementId)
            ) {
                StampRallyImagePager(
                    pagerState = pagerState,
                    images = images,
                    ignored = ignored,
                    sharedElementId = sharedElementId,
                    blockCrossAxisScrolling = blockCrossAxisScrolling,
                    onClick = onClick,
                    onClickFullscreen = onClickFullscreen,
                )
                StampRallyListRow(
                    stampRallyWithUserData = stampRallyWithUserData,
                    onFavoriteToggle = onFavoriteToggle,
                    seriesImage = seriesImage,
                )
            }
        SearchDisplayType.IMAGE -> {
            SearchItemImage(
                images = images,
                ignored = ignored,
                onIgnoredToggle = onIgnoredToggle,
                onClick = { onClick(pagerState.settledPage) },
                imagePager = {
                    Box {
                        StampRallyImagePager(
                            pagerState = pagerState,
                            images = images,
                            ignored = ignored,
                            sharedElementId = sharedElementId,
                            blockCrossAxisScrolling = blockCrossAxisScrolling,
                            clipCorners = false,
                            onClick = onClick,
                            onClickFullscreen = onClickFullscreen,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(topEnd = 12.dp)
                                )
                        ) {
                            val booth = stampRally.hostTable
                            Text(
                                text = booth,
                                style = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                                    .sharedElement(
                                        "booth",
                                        sharedElementId,
                                        zIndexInOverlay = 1f
                                    )
                            )

                            FavoriteIconButton(
                                entryText = { "${stampRally.hostTable}-${stampRally.fandom}" },
                                favorite = { userEntry.favorite },
                                onFavoriteToggle = onFavoriteToggle,
                                modifier = Modifier
                                    .sharedElement(
                                        "favorite",
                                        sharedElementId,
                                        zIndexInOverlay = 1f
                                    )
                            )
                        }
                    }
                },
                noImageContent = {
                    StampRallyListRow(
                        stampRallyWithUserData = stampRallyWithUserData,
                        onFavoriteToggle = onFavoriteToggle,
                        seriesImage = seriesImage,
                    )
                },
                modifier = Modifier.sharedBounds("itemContainer", sharedElementId),
            )
        }
        SearchDisplayType.TABLE -> throw IllegalArgumentException()
    }
}

@Composable
private fun StampRallyImagePager(
    pagerState: PagerState,
    images: List<ImageWithDimensions>,
    ignored: Boolean,
    sharedElementId: Any,
    blockCrossAxisScrolling: () -> Boolean,
    onClick: ((Int) -> Unit)?,
    onClickFullscreen: ((index: Int) -> Unit)?,
    modifier: Modifier = Modifier,
    clipCorners: Boolean = true,
) {
    if (images.isNotEmpty() && !ignored) {
        Column(modifier = modifier) {
            ImagePager(
                images = images,
                pagerState = pagerState,
                sharedElementId = sharedElementId,
                blockCrossAxisScrolling = blockCrossAxisScrolling,
                onClickPage = onClick,
                onClickFullscreen = onClickFullscreen,
                clipCorners = clipCorners,
            )
        }
    }
}

@Composable
private fun Preview(displayType: SearchDisplayType) {
    val stampRallyWithUserData = StampRallyWithUserDataProvider.values.first()
    StampRallySearchItem(
        displayType = displayType,
        stampRallyWithUserData = stampRallyWithUserData,
        showGridByDefault = false,
        showRandomCatalogImage = false,
        blockCrossAxisScrolling = { false },
        onFavoriteToggle = {},
        onIgnoredToggle = {},
        onClick = {},
        onClickFullscreen = {},
        seriesImage = { it.id },
    )
}

@AlleyPreview
@Composable
private fun StampRallySearchItemCardPreview() = Preview(SearchDisplayType.CARD)

@AlleyPreview
@Composable
private fun StampRallySearchItemImagePreview() = Preview(SearchDisplayType.IMAGE)

@AlleyPreview
@Composable
private fun StampRallySearchItemListPreview() = Preview(SearchDisplayType.LIST)
