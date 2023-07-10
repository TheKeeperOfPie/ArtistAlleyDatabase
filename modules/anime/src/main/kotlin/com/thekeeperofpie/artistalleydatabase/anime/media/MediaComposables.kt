@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.graphics.ColorUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.fragment.AniListListRowMedia
import com.anilist.fragment.MediaNavigationData
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeEnd
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

fun <T> LazyListScope.mediaListSection(
    screenKey: String,
    @StringRes titleRes: Int,
    values: Collection<T>,
    valueToEntry: (T) -> AnimeMediaListRow.Entry,
    aboveFold: Int,
    expanded: () -> Boolean,
    onExpandedChange: (Boolean) -> Unit,
    colorCalculationState: ColorCalculationState,
    navigationCallback: AnimeNavigator.NavigationCallback,
    onTagLongClick: (String) -> Unit,
    label: (@Composable (T) -> Unit)? = null,
) = listSection(
    titleRes = titleRes,
    values = values,
    valueToId = { valueToEntry(it).id?.scopedId },
    aboveFold = aboveFold,
    expanded = expanded,
    onExpandedChange = onExpandedChange,
) { item, paddingBottom, modifier ->
    val entry = valueToEntry(item)
    AnimeMediaListRow(
        screenKey = screenKey,
        entry = entry,
        label = if (label == null) null else {
            { label(item) }
        },
        onTagLongClick = onTagLongClick,
        colorCalculationState = colorCalculationState,
        navigationCallback = navigationCallback,
        modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
    )
}

fun LazyListScope.mediaHorizontalRow(
    screenKey: String,
    @StringRes titleRes: Int,
    entries: List<MediaNavigationData>,
    onClickEntry: (MediaNavigationData, imageWidthToHeightRatio: Float) -> Unit,
    colorCalculationState: ColorCalculationState,
    imageHeight: Dp = 180.dp,
    showTitle: Boolean = true,
    sectionTitle: @Composable () -> Unit = {
        DetailsSectionHeader(stringResource(titleRes))
    }
) {
    if (entries.isEmpty()) return
    item { sectionTitle() }

    item {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(entries, { it.id }) {
                val id = it.id.toString()
                val colors = colorCalculationState.colorMap[id]
                val animationProgress by animateIntAsState(
                    if (colors == null) 0 else 255,
                    label = "Media card color fade in",
                )

                val containerColor = when {
                    colors == null || animationProgress == 0 ->
                        MaterialTheme.colorScheme.surface
                    animationProgress == 255 -> colors.first
                    else -> Color(
                        ColorUtils.compositeColors(
                            ColorUtils.setAlphaComponent(
                                colors.first.toArgb(),
                                animationProgress
                            ),
                            MaterialTheme.colorScheme.surface.toArgb()
                        )
                    )
                }

                var widthToHeightRatio by remember { MutableSingle<Float?>(null) }
                ElevatedCard(
                    onClick = { onClickEntry(it, widthToHeightRatio ?: 1f) },
                    colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
                    modifier = Modifier.animateItemPlacement(),
                ) {
                    ConstraintLayout {
                        val (image, title) = createRefs()
                        Box(
                            modifier = Modifier
                                .constrainAs(image) {
                                    height =
                                        androidx.constraintlayout.compose.Dimension
                                            .value(imageHeight)
                                    width =
                                        androidx.constraintlayout.compose.Dimension.wrapContent
                                    linkTo(start = parent.start, end = parent.end)
                                    top.linkTo(parent.top)
                                }
                        ) {
                            SharedElement(
                                key = "anime_media_${it.id}_image",
                                screenKey = screenKey,
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(it.coverImage?.extraLarge)
                                        .crossfade(true)
                                        .allowHardware(colorCalculationState.hasColor(id))
                                        .size(
                                            width = Dimension.Undefined,
                                            height = Dimension.Pixels(
                                                LocalDensity.current.run { imageHeight.roundToPx() }
                                            ),
                                        )
                                        .build(),
                                    contentScale = ContentScale.FillHeight,
                                    contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
                                    onSuccess = {
                                        widthToHeightRatio = it.widthToHeightRatio()
                                        ComposeColorUtils.calculatePalette(
                                            id = id,
                                            success = it,
                                            colorCalculationState = colorCalculationState,
                                            heightStartThreshold = 3 / 4f,
                                            selectMaxPopulation = true,
                                        )
                                    },
                                    modifier = Modifier
                                        .height(imageHeight)
                                        .conditionally(widthToHeightRatio != null) {
                                            widthIn(max = imageHeight)
                                        }
                                )
                            }
                        }

                        if (showTitle) {
                            AutoHeightText(
                                text = it.title?.userPreferred.orEmpty(),
                                color = ComposeColorUtils.bestTextColor(containerColor)
                                    ?: Color.Unspecified,
                                maxLines = 2,
                                minLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .constrainAs(title) {
                                        linkTo(start = image.start, end = image.end)
                                        top.linkTo(image.bottom)
                                        width =
                                            androidx.constraintlayout.compose.Dimension.fillToConstraints
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaRatingIconsSection(
    rating: Int?,
    popularity: Int?,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    if (rating == null && popularity == null) return
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
        modifier = modifier,
    ) {
        if (rating != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .placeholder(
                        visible = loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    ),
            ) {
                Text(
                    text = rating.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                val iconTint = remember(rating) {
                    when {
                        rating > 80 -> Color.Green
                        rating > 70 -> Color.Yellow
                        rating > 50 -> Color(0xFFFF9000) // Orange
                        else -> Color.Red
                    }
                }
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = stringResource(
                        R.string.anime_media_rating_icon_content_description
                    ),
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (popularity != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .padding(end = 4.dp)
                    .placeholder(
                        visible = loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    ),
            ) {
                Text(
                    text = popularity.toString(),
                    style = MaterialTheme.typography.labelSmall,
                )

                Icon(
                    imageVector = when {
                        popularity > 100000 -> Icons.Filled.PeopleAlt
                        popularity > 50000 -> Icons.Outlined.PeopleAlt
                        popularity > 10000 -> Icons.Filled.Person
                        else -> Icons.Filled.PersonOutline
                    },
                    contentDescription = stringResource(
                        R.string.anime_media_rating_population_icon_content_description
                    ),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun MediaNextAiringSection(
    nextAiringEpisode: AniListListRowMedia.NextAiringEpisode,
    loading: Boolean
) {
    val context = LocalContext.current
    val airingAt = remember(nextAiringEpisode.id) {
        MediaUtils.formatAiringAt(context, nextAiringEpisode.airingAt * 1000L)
    }

    // TODO: De-dupe airingAt and remainingTime if both show a specific date
    //  (airing > 7 days away)
    val remainingTime = remember(nextAiringEpisode.id) {
        MediaUtils.formatRemainingTime(nextAiringEpisode.airingAt * 1000L)
    }

    Text(
        text = stringResource(
            R.string.anime_media_next_airing_episode,
            nextAiringEpisode.episode,
            airingAt,
            remainingTime,
        ),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.typography.labelSmall.color
            .takeOrElse { LocalContentColor.current }
            .copy(alpha = 0.8f),
        modifier = Modifier
            .wrapContentHeight(Alignment.Bottom)
            .padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 4.dp)
            .placeholder(
                visible = loading,
                highlight = PlaceholderHighlight.shimmer(),
            )
    )
}

@Composable
fun MediaTagRow(
    tags: List<AnimeMediaTagEntry>,
    onTagClick: (tagId: String, tagName: String) -> Unit,
    onTagLongClick: (tagId: String) -> Unit,
    tagContainerColor: Color,
    tagTextColor: Color,
) {
    LazyRow(
        contentPadding = PaddingValues(start = 12.dp, end = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(top = 4.dp, bottom = 10.dp)
            .fillMaxWidth()
            // SubcomposeLayout doesn't support fill max width, so use a really large number.
            // The parent will clamp the actual width so all content still fits on screen.
            .size(width = LocalConfiguration.current.screenWidthDp.dp, height = 24.dp)
            .fadingEdgeEnd(
                endOpaque = 32.dp,
                endTransparent = 16.dp,
            )
    ) {
        items(tags, { it.id }) {
            AnimeMediaTagEntry.Chip(
                tag = it,
                onTagClick = onTagClick,
                onTagLongClick = onTagLongClick,
                containerColor = tagContainerColor,
                textColor = tagTextColor,
                modifier = Modifier.height(24.dp),
            )
        }
    }
}
