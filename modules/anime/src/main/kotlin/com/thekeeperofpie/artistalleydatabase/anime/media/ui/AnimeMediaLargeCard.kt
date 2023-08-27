package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Dimension
import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.type.MediaType
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.ignore.LocalIgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.primaryTitle
import com.thekeeperofpie.artistalleydatabase.anime.ui.blurForScreenshotMode
import com.thekeeperofpie.artistalleydatabase.compose.AppThemeSetting
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.CustomHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.LocalAppTheme
import com.thekeeperofpie.artistalleydatabase.compose.LocalColorCalculationState
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@OptIn(ExperimentalFoundationApi::class)
object AnimeMediaLargeCard {

    private val HEIGHT = 200.dp

    @Composable
    operator fun invoke(
        screenKey: String,
        viewer: AniListViewer?,
        entry: Entry?,
        modifier: Modifier = Modifier,
        label: (@Composable () -> Unit)? = null,
        onClickListEdit: (Entry) -> Unit,
        forceListEditIcon: Boolean = false,
        showQuickEdit: Boolean = true,
    ) {
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = HEIGHT)
                .alpha(if (entry?.ignored == true) 0.38f else 1f)
        ) {
            val navigationCallback = LocalNavigationCallback.current
            val ignoreController = LocalIgnoreController.current
            Box(
                modifier = Modifier.combinedClickable(
                    enabled = entry != null,
                    onClick = {
                        if (entry != null) navigationCallback.onMediaClick(entry.media)
                    },
                    onLongClick = {
                        if (entry?.media != null) {
                            ignoreController.toggle(entry.media)
                        }
                    }
                )
            ) {
                BannerImage(
                    screenKey = screenKey,
                    entry = entry,
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .heightIn(min = HEIGHT)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            label?.invoke()
                            TitleText(entry)
                            SubtitleText(entry)
                        }

                        MediaRatingIconsSection(
                            rating = entry?.rating,
                            popularity = entry?.popularity,
                            loading = entry == null,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .wrapContentWidth()
                        )
                    }

                    val description = entry?.description
                    if (description == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        CustomHtmlText(
                            text = description,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                            fontWeight = FontWeight.Black,
                            overflow = TextOverflow.Ellipsis,
                            detectTaps = false,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Column(modifier = Modifier.weight(1f)) {
                            entry?.nextAiringEpisode?.let {
                                MediaNextAiringSection(
                                    nextAiringEpisode = it,
                                    episodes = entry.media.episodes,
                                    format = entry.media.format,
                                )
                            }
                            val colorCalculationState = LocalColorCalculationState.current
                            val (containerColor, textColor) =
                                colorCalculationState.getColors(entry?.id?.valueId)

                            MediaTagRow(
                                tags = entry?.tags.orEmpty(),
                                onTagClick = { id, name ->
                                    if (entry != null) {
                                        navigationCallback.onTagClick(
                                            entry.media.type ?: MediaType.ANIME,
                                            id,
                                            name
                                        )
                                    }
                                },
                                tagContainerColor = containerColor,
                                tagTextColor = textColor,
                            )
                        }

                        if (viewer != null && entry != null && showQuickEdit) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp, bottom = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                MediaListQuickEditIconButton(
                                    viewer = viewer,
                                    mediaType = entry.media.type,
                                    media = entry,
                                    maxProgress = MediaUtils.maxProgress(entry.media),
                                    maxProgressVolumes = entry.media.volumes,
                                    forceListEditIcon = forceListEditIcon,
                                    onClick = { onClickListEdit(entry) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BannerImage(
        screenKey: String,
        entry: Entry?,
    ) {
        SharedElement(
            key = "${entry?.id?.scopedId}_banner_image",
            screenKey = screenKey,
        ) {
            val foregroundColor = MaterialTheme.colorScheme.surface
            var loaded by remember(entry?.id?.valueId) { mutableStateOf(false) }
            val alpha by animateFloatAsState(
                if (loaded) 1f else 0f,
                label = "AnimeMediaLargeCard banner image alpha",
            )
            val appTheme = LocalAppTheme.current
            val isLightTheme = appTheme == AppThemeSetting.LIGHT
                    || (appTheme == AppThemeSetting.AUTO && !isSystemInDarkTheme())
            val colorCalculationState = LocalColorCalculationState.current
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(entry?.imageBanner ?: entry?.image)
                    .crossfade(true)
                    .allowHardware(colorCalculationState.hasColor(entry?.id?.valueId))
                    .size(
                        width = Dimension.Undefined,
                        height = Dimension.Pixels(
                            LocalDensity.current.run { HEIGHT.roundToPx() / 4 * 3 }
                        ),
                    )
                    .build(),
                contentScale = ContentScale.Crop,
                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                onSuccess = {
                    loaded = true
                    if (entry != null) {
                        ComposeColorUtils.calculatePalette(
                            entry.id.valueId,
                            it,
                            colorCalculationState,
                        )
                    }
                },
                contentDescription = stringResource(
                    R.string.anime_media_banner_image_content_description
                ),
                modifier = Modifier
                    .background(entry?.color ?: MaterialTheme.colorScheme.surfaceVariant)
                    .fillMaxWidth()
                    .height(HEIGHT)
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            foregroundColor,
                            alpha = if (isLightTheme) 0.8f else 0.6f,
                        )
                    }
                    // Clip to match card so that shared element animation keeps rounded corner
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .alpha(alpha)
                    .blurForScreenshotMode()
            )
        }
    }

    @Composable
    private fun TitleText(entry: Entry?) {
        Text(
            text = entry?.media?.title?.primaryTitle() ?: "Loading...",
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top)
                .padding(start = 12.dp, top = 10.dp, end = 16.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun SubtitleText(entry: Entry?) {
        val media = entry?.media
        Text(
            text = if (entry == null) "Loading..." else MediaUtils.formatSubtitle(
                format = media?.format,
                status = media?.status,
                season = media?.season,
                seasonYear = media?.seasonYear,
            ),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current }
                .copy(alpha = 0.8f),
            modifier = Modifier
                .wrapContentHeight()
                .padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 4.dp)
                .placeholder(
                    visible = entry == null,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    interface Entry : MediaStatusAware {
        val media: MediaPreviewWithDescription
        val id: EntryId
            get() = EntryId("anime_media", media.id.toString())
        val image: String?
            get() = media.coverImage?.extraLarge
        val imageBanner: String?
            get() = media.bannerImage
        val color: Color?
            get() = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)

        val rating: Int?
            get() = media.averageScore
        val popularity: Int?
            get() = media.popularity

        val nextAiringEpisode: MediaPreviewWithDescription.NextAiringEpisode?
            get() = media.nextAiringEpisode

        val tags: List<AnimeMediaTagEntry>

        val description: String?
    }
}
