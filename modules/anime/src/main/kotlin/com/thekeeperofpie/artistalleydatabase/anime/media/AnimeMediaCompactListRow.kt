package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.fragment.MediaCompactWithTags
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio

@OptIn(ExperimentalFoundationApi::class)
object AnimeMediaCompactListRow {

    private val DEFAULT_IMAGE_HEIGHT = 100.dp
    private val DEFAULT_IMAGE_WIDTH = 72.dp

    @Composable
    operator fun invoke(
        screenKey: String,
        entry: Entry?,
        modifier: Modifier = Modifier,
        onLongClick: (Entry) -> Unit,
        onTagLongClick: (tagId: String) -> Unit,
        onLongPressImage: (Entry) -> Unit,
        colorCalculationState: ColorCalculationState,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
        OutlinedCard(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = DEFAULT_IMAGE_HEIGHT)
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
        ) {
            Row(modifier = Modifier
                .height(IntrinsicSize.Min)
                .combinedClickable(
                    enabled = entry != null,
                    onClick = {
                        if (entry != null) {
                            navigationCallback.onMediaClick(
                                entry.media,
                                imageWidthToHeightRatio
                            )
                        }
                    },
                    onLongClick = { if (entry != null) onLongClick(entry) }
                )
            ) {
                CoverImage(
                    screenKey = screenKey,
                    entry = entry,
                    onClick = {
                        if (entry != null) {
                            navigationCallback.onMediaClick(entry.media, imageWidthToHeightRatio)
                        }
                    },
                    onLongPressImage = onLongPressImage,
                    colorCalculationState = colorCalculationState,
                    onRatioAvailable = { imageWidthToHeightRatio = it }
                )

                Column(modifier = Modifier.height(DEFAULT_IMAGE_HEIGHT)) {
                    Row(Modifier.fillMaxWidth().weight(1f)) {
                        TitleText(entry, modifier = Modifier.weight(1f))

                        MediaRatingIconsSection(
                            rating = entry?.media?.averageScore,
                            popularity = entry?.media?.popularity,
                            loading = entry == null,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .wrapContentWidth()
                        )
                    }

                    val (containerColor, textColor) =
                        colorCalculationState.getColors(entry?.media?.id?.toString())
                    MediaTagRow(
                        tags = entry?.tags.orEmpty(),
                        onTagClick = navigationCallback::onTagClick,
                        onTagLongClick = onTagLongClick,
                        tagContainerColor = containerColor,
                        tagTextColor = textColor,
                        tagTextStyle = MaterialTheme.typography.bodySmall,
                        height = 20.dp,
                    )
                }
            }
        }
    }

    @Composable
    private fun CoverImage(
        screenKey: String,
        entry: Entry?,
        onClick: (Entry) -> Unit = {},
        onLongPressImage: (entry: Entry) -> Unit,
        colorCalculationState: ColorCalculationState,
        onRatioAvailable: (Float) -> Unit,
    ) {
        SharedElement(
            key = "anime_media_${entry?.media?.id}_image",
            screenKey = screenKey,
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(entry?.media?.coverImage?.extraLarge)
                    .crossfade(true)
                    .allowHardware(colorCalculationState.hasColor(entry?.media?.id?.toString()))
                    .size(
                        width = Dimension.Pixels(
                            LocalDensity.current.run { DEFAULT_IMAGE_WIDTH.roundToPx() }
                        ),
                        height = Dimension.Undefined
                    )
                    .build(),
                contentScale = ContentScale.Crop,
                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                onSuccess = {
                    onRatioAvailable(it.widthToHeightRatio())
                    ComposeColorUtils.calculatePalette(
                        entry?.media?.id.toString(),
                        it,
                        colorCalculationState,
                    )
                },
                contentDescription = stringResource(R.string.anime_media_cover_image_content_description),
                modifier = Modifier
                    // Clip to match card so that shared element animation keeps rounded corner
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .fillMaxHeight()
                    .size(width = DEFAULT_IMAGE_WIDTH, height = DEFAULT_IMAGE_HEIGHT)
                    .placeholder(
                        visible = entry == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .combinedClickable(
                        onClick = { if (entry != null) onClick(entry) },
                        onLongClick = { if (entry != null) onLongPressImage(entry) },
                        onLongClickLabel = stringResource(
                            R.string.anime_media_cover_image_long_press_preview
                        ),
                    )
            )
        }
    }

    @Composable
    private fun TitleText(entry: Entry?, modifier: Modifier = Modifier) {
        Text(
            text = entry?.media?.title?.userPreferred ?: "Loading...",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
                .wrapContentHeight(Alignment.Top)
                .padding(start = 12.dp, top = 8.dp, end = 16.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    class Entry(
        val media: MediaCompactWithTags,
        ignored: Boolean,
    ) {
        val tags = media.tags?.filterNotNull()
            ?.map { AnimeMediaTagEntry(it, isMediaSpoiler = it.isMediaSpoiler, rank = it.rank) }
            .orEmpty()
        val ignored by mutableStateOf(ignored)
    }
}
