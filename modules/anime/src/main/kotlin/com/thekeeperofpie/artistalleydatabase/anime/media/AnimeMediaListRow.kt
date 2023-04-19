package com.thekeeperofpie.artistalleydatabase.anime.media

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.anilist.fragment.AniListListRowMedia
import com.anilist.type.MediaSeason
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.mxalbert.sharedelements.SharedElement
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdge
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

@OptIn(ExperimentalFoundationApi::class)
object AnimeMediaListRow {

    @Composable
    operator fun invoke(
        entry: Entry,
        modifier: Modifier = Modifier,
        label: (@Composable () -> Unit)? = null,
        onClick: (Entry) -> Unit = {},
        onTagClick: (tagId: String, tagName: String) -> Unit = { _, _ -> },
        onTagLongClick: (tagId: String) -> Unit = {},
        onLongPressImage: (entry: Entry) -> Unit = {},
    ) {
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
                .clickable(enabled = entry != Entry.Loading, onClick = { onClick(entry) }),
        ) {
            Row {
                CoverImage(entry, onLongPressImage)

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
                    TagRow(
                        tags = entry.tags,
                        onTagClick = onTagClick,
                        onTagLongClick = onTagLongClick,
                    )
                }
            }
        }
    }

    @Composable
    private fun CoverImage(entry: Entry, onLongPressImage: (entry: Entry) -> Unit) {
        SharedElement(
            key = "cover_image_${entry.id?.valueId}",
            screenKey = "media_row"
        ) {
            AsyncImage(
                model = entry.image,
                contentScale = ContentScale.FillHeight,
                fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
                contentDescription = stringResource(R.string.anime_media_cover_image),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .fillMaxHeight()
                    .heightIn(min = 180.dp)
                    .width(120.dp)
                    .placeholder(
                        visible = entry == Entry.Loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                    .combinedClickable(
                        onClick = {},
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
                stringResource(entry.subtitleMediaFormatRes),
                stringResource(entry.subtitleStatusRes),
                MediaUtils.formatSeasonYear(
                    entry.subtitleSeason,
                    entry.subtitleYear,
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
            DateUtils.formatDateTime(
                context,
                nextAiringEpisode.airingAt * 1000L,
                MediaUtils.BASE_DATE_FORMAT_FLAGS or DateUtils.FORMAT_SHOW_DATE or
                        DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_TIME
            )
        }

        // TODO: De-dupe airingAt and remainingTime if both show a specific date
        //  (airing > 7 days away)
        val remainingTime = remember(nextAiringEpisode.id) {
            DateUtils.getRelativeTimeSpanString(
                nextAiringEpisode.airingAt * 1000L,
                System.currentTimeMillis(),
                0,
                MediaUtils.BASE_DATE_FORMAT_FLAGS,
            )
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
        onTagClick: (tagId: String, tagName: String) -> Unit = { _, _ -> },
        onTagLongClick: (tagId: String) -> Unit = {},
    ) {
        LazyRow(
            contentPadding = PaddingValues(start = 12.dp, end = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(top = 4.dp, bottom = 10.dp)
                .fadingEdge(
                    endOpaque = 32.dp,
                    endTransparent = 16.dp,
                )
        ) {
            items(tags, { it.id }) {
                AnimeMediaTagEntry.Chip(
                    tag = it,
                    onTagClicked = onTagClick,
                    onTagLongClicked = onTagLongClick,
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
        }

        val id: EntryId? get() = null
        val image: String? get() = null
        val imageExtraLarge: String? get() = image
        val imageBanner: String? get() = null
        val color: Color? get() = null
        val title: String?

        val subtitleMediaFormatRes: Int get() = R.string.anime_media_format_tv
        val subtitleStatusRes: Int get() = R.string.anime_media_status_finished
        val subtitleSeason: MediaSeason? get() = null
        val subtitleYear: Int? get() = 2023

        val rating: Int? get() = 99
        val popularity: Int? get() = 12345

        val nextAiringEpisode: AniListListRowMedia.NextAiringEpisode? get() = null

        val tags: List<AnimeMediaTagEntry> get() = emptyList()
    }

    open class MediaEntry(
        val media: AniListListRowMedia
    ) : Entry {

        override val id = EntryId("item", media.id.toString())
        override val image = media.coverImage?.large
        override val imageExtraLarge = media.coverImage?.extraLarge
        override val imageBanner = media.bannerImage
        override val color = media.coverImage?.color?.let(ColorUtils::hexToColor)
        override val title = media.title?.userPreferred

        override val subtitleMediaFormatRes = media.format.toTextRes()
        override val subtitleStatusRes = media.status.toTextRes()
        override val subtitleSeason = media.season
        override val subtitleYear = media.seasonYear

        override val rating = media.averageScore
        override val popularity = media.popularity

        override val nextAiringEpisode = media.nextAiringEpisode

        override val tags = media.tags?.filterNotNull()?.map(::AnimeMediaTagEntry).orEmpty()
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
    })
}