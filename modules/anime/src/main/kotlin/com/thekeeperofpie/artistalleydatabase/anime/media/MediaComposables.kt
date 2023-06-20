@file:OptIn(ExperimentalMaterial3Api::class)

package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
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
import com.anilist.fragment.MediaNavigationData
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ui.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
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
    aboveFold = aboveFold,
    expanded = expanded,
    onExpandedChange = onExpandedChange,
) { item, paddingBottom ->
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
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
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
                                    modifier = Modifier.height(imageHeight)
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
