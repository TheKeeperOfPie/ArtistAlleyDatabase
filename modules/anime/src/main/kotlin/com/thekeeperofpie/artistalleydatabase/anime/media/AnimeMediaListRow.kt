package com.thekeeperofpie.artistalleydatabase.anime.media

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.anilist.fragment.AniListListRowMedia
import com.anilist.type.MediaSeason
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.android_utils.MutableSingle
import com.thekeeperofpie.artistalleydatabase.android_utils.getValue
import com.thekeeperofpie.artistalleydatabase.android_utils.setValue
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.ComposeColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeEnd
import com.thekeeperofpie.artistalleydatabase.compose.widthToHeightRatio
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@OptIn(ExperimentalFoundationApi::class)
object AnimeMediaListRow {

    @Composable
    operator fun invoke(
        entry: Entry,
        modifier: Modifier = Modifier,
        label: (@Composable () -> Unit)? = null,
        onLongClick: (Entry) -> Unit = {},
        onTagLongClick: (tagId: String) -> Unit = {},
        onLongPressImage: (entry: Entry) -> Unit = {},
        colorCalculationState: ColorCalculationState = ColorCalculationState(),
        navigationCallback: AnimeNavigator.NavigationCallback =
            AnimeNavigator.NavigationCallback(null),
    ) {
        var imageWidthToHeightRatio by remember { MutableSingle(1f) }
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .combinedClickable(
                    enabled = entry != Entry.Loading,
                    onClick = { navigationCallback.onMediaClick(entry, imageWidthToHeightRatio) },
                    onLongClick = { onLongClick(entry) }
                )
                .alpha(if (entry.ignored) 0.38f else 1f)
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                CoverImage(
                    entry = entry,
                    onClick = { navigationCallback.onMediaClick(entry, imageWidthToHeightRatio) },
                    onLongPressImage = onLongPressImage,
                    colorCalculationState = colorCalculationState,
                    onRatioAvailable = { imageWidthToHeightRatio = it}
                )

                Column(modifier = Modifier.heightIn(min = 180.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            label?.invoke()
                            TitleText(entry)
                            SubtitleText(entry)
                        }

                        RatingSection(entry, modifier = Modifier.wrapContentWidth())
                    }

                    Spacer(Modifier.weight(1f))

                    entry.nextAiringEpisode?.let {
                        NextAiringSection(it, entry == Entry.Loading)
                    }
                    val (containerColor, textColor) =
                        colorCalculationState.getColors(entry.id?.valueId)
                    TagRow(
                        tags = entry.tags,
                        onTagClick = navigationCallback::onTagClick,
                        onTagLongClick = onTagLongClick,
                        tagContainerColor = containerColor,
                        tagTextColor = textColor,
                    )
                }
            }
        }
    }

    @Composable
    private fun CoverImage(
        entry: Entry,
        onClick: (Entry) -> Unit = {},
        onLongPressImage: (entry: Entry) -> Unit,
        colorCalculationState: ColorCalculationState,
        onRatioAvailable: (Float) -> Unit,
    ) {
        SharedElement(
            key = "cover_image_${entry.id?.valueId}",
            screenKey = "media_row_${entry.id?.valueId}"
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(entry.image)
                    .crossfade(true)
                    .allowHardware(false)
                    .build(),
                contentScale = ContentScale.Crop,
                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                onSuccess = {
                    onRatioAvailable(it.widthToHeightRatio())
                    ComposeColorUtils.calculatePalette(
                        entry.id?.valueId.orEmpty(),
                        it,
                        colorCalculationState,
                    )
                },
                contentDescription = stringResource(R.string.anime_media_cover_image),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .fillMaxHeight()
                    .heightIn(min = 180.dp)
                    .width(130.dp)
                    .placeholder(
                        visible = entry == Entry.Loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .combinedClickable(
                        onClick = { onClick(entry) },
                        onLongClick = { onLongPressImage(entry) },
                        onLongClickLabel = stringResource(
                            R.string.anime_media_cover_image_long_press_preview
                        ),
                    )
            )
        }
    }

    @Composable
    private fun TitleText(entry: Entry) {
        Text(
            text = entry.title ?: "Loading...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(Alignment.Top)
                .padding(start = 12.dp, top = 10.dp, end = 16.dp)
                .placeholder(
                    visible = entry == Entry.Loading,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun SubtitleText(entry: Entry) {
        Text(
            text = listOfNotNull(
                stringResource(entry.subtitleFormatRes),
                stringResource(entry.subtitleStatusRes),
                MediaUtils.formatSeasonYear(
                    entry.subtitleSeason,
                    entry.subtitleSeasonYear,
                    withSeparator = true
                ),
            ).joinToString(separator = " - "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current }
                .copy(alpha = 0.8f),
            modifier = Modifier
                .wrapContentHeight()
                .padding(start = 12.dp, top = 4.dp, end = 16.dp, bottom = 10.dp)
                .placeholder(
                    visible = entry == Entry.Loading,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun RatingSection(entry: Entry, modifier: Modifier = Modifier) {
        val rating = entry.rating
        val popularity = entry.popularity
        val loading = entry == Entry.Loading
        if (rating == null && popularity == null) return
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End,
            modifier = modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            if (rating != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(24.dp)
                        .placeholder(
                            visible = loading,
                            highlight = PlaceholderHighlight.shimmer(),
                        ),
                ) {
                    AutoHeightText(
                        text = rating.toString(),
                        style = MaterialTheme.typography.labelLarge,
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
                    )
                }
            }

            if (popularity != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(16.dp)
                        .padding(end = 4.dp)
                        .placeholder(
                            visible = loading,
                            highlight = PlaceholderHighlight.shimmer(),
                        ),
                ) {
                    AutoHeightText(
                        text = popularity.toString(),
                        style = MaterialTheme.typography.labelLarge,
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
                    )
                }
            }
        }
    }

    @Composable
    private fun NextAiringSection(
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
    private fun TagRow(
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
                .size(width = 39393.dp, height = 24.dp)
                .fadingEdgeEnd(
                    endOpaque = 32.dp,
                    endTransparent = 16.dp,
                )
        ) {
            items(tags, { it.id }) {
                AnimeMediaTagEntry.Chip(
                    tag = it,
                    onTagClicked = onTagClick,
                    onTagLongClicked = onTagLongClick,
                    containerColor = tagContainerColor,
                    textColor = tagTextColor,
                    modifier = Modifier.height(24.dp),
                )
            }
        }
    }

    interface Entry {
        object Loading : Entry {
            override val id = null
            override val image = null
            override val title = ""
            override val tags = emptyList<AnimeMediaTagEntry>()
            override var ignored = false
        }

        val id: EntryId? get() = null
        val image: String? get() = null
        val imageExtraLarge: String? get() = image
        val imageBanner: String? get() = null
        val color: Color? get() = null
        val title: String?

        val subtitleFormatRes: Int get() = R.string.anime_media_format_tv
        val subtitleStatusRes: Int get() = R.string.anime_media_status_finished
        val subtitleSeason: MediaSeason? get() = null
        val subtitleSeasonYear: Int? get() = 2023

        val rating: Int? get() = 99
        val popularity: Int? get() = 12345

        val nextAiringEpisode: AniListListRowMedia.NextAiringEpisode? get() = null

        val tags: List<AnimeMediaTagEntry> get() = emptyList()

        var ignored: Boolean
    }

    open class MediaEntry<MediaType : AniListListRowMedia>(
        val media: MediaType,
        ignored: Boolean = false
    ) : Entry {
        override val id = EntryId("item", media.id.toString())
        override val image = media.coverImage?.large
        override val imageExtraLarge = media.coverImage?.extraLarge
        override val imageBanner = media.bannerImage
        override val color = media.coverImage?.color?.let(ComposeColorUtils::hexToColor)
        override val title = media.title?.userPreferred

        override val subtitleFormatRes = media.format.toTextRes()
        override val subtitleStatusRes = media.status.toTextRes()
        override val subtitleSeason = media.season
        override val subtitleSeasonYear = media.seasonYear

        override val rating = media.averageScore
        override val popularity = media.popularity

        override val nextAiringEpisode = media.nextAiringEpisode

        override val tags = media.tags?.filterNotNull()?.map(::AnimeMediaTagEntry).orEmpty()

        override var ignored by mutableStateOf(ignored)
    }
}

@Preview
@Composable
private fun Preview() {
    AnimeMediaListRow(object : AnimeMediaListRow.Entry {
        override val image = null
        override val title =
            "Tsundere Akuyaku Reijou Liselotte to Jikkyou no Endou-kun to Kaisetsu no Kobayashi-san"
        override val tags: List<AnimeMediaTagEntry> = listOf(
            AnimeMediaTagEntry(
                id = "857",
                name = "Villainess",
            ),
            AnimeMediaTagEntry(
                id = "164",
                name = "Tsundere",
            ),
            AnimeMediaTagEntry(
                id = "85",
                shouldHide = true,
                leadingIconVector = Icons.Filled.Warning,
                leadingIconContentDescription = R.string.anime_media_tag_is_spoiler,
                name = "Tragedy",
                textHiddenRes = R.string.anime_media_tag_spoiler,
            ),
        )
        override var ignored = false
    })
}
