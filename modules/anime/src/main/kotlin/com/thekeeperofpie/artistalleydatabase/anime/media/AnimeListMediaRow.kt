package com.thekeeperofpie.artistalleydatabase.anime.media

import android.text.format.DateUtils
import androidx.compose.foundation.background
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toTextRes
import com.thekeeperofpie.artistalleydatabase.compose.AutoHeightText
import com.thekeeperofpie.artistalleydatabase.compose.ColorUtils
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdge
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

object AnimeListMediaRow {

    @Composable
    operator fun invoke(
        entry: Entry,
        onTagClick: (tagId: String, tagName: String) -> Unit = { _, _ -> },
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
        ) {
            Row {
                CoverImage(entry)

                Column(modifier = Modifier.heightIn(min = 180.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            TitleText(entry)
                            SubtitleText(entry)
                        }

                        RatingSection(entry, modifier = Modifier.wrapContentWidth())
                    }

                    Spacer(Modifier.weight(1f))

                    entry.nextAiringEpisode?.let {
                        NextAiringSection(it, entry == Entry.Loading)
                    }
                    TagRow(tags = entry.tags, onTagClick = onTagClick)
                }
            }
        }
    }

    @Composable
    private fun CoverImage(entry: Entry) {
        AsyncImage(
            model = entry.image,
            contentScale = ContentScale.Crop,
            fallback = rememberVectorPainter(Icons.Filled.ImageNotSupported),
            contentDescription = stringResource(R.string.anime_media_cover_image),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .heightIn(min = 180.dp)
                .width(120.dp)
                .fillMaxHeight()
                .placeholder(
                    visible = entry == Entry.Loading,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
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
                entry.subtitleSeason?.toTextRes()
                    ?.let { stringResource(it) + " " }
                    .orEmpty() + entry.subtitleYear,
            ).joinToString(separator = " - "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.typography.bodySmall.color
                .takeOrElse { LocalContentColor.current }
                .copy(alpha = 0.8f),
            modifier = Modifier.wrapContentHeight()
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
                DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY
            )
        }

        // TODO: De-dupe airingAt and remainingTime if both show a specific date
        //  (airing > 7 days away)
        val remainingTime = remember(nextAiringEpisode.id) {
            DateUtils.getRelativeTimeSpanString(
                nextAiringEpisode.airingAt * 1000L,
                System.currentTimeMillis(),
                0,
                DateUtils.FORMAT_ABBREV_RELATIVE
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
        tags: List<Tag>,
        onTagClick: (tagId: String, tagName: String) -> Unit = { _, _ -> },
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
                val shouldHide = it.shouldHide
                var hidden by remember(it.id) { mutableStateOf(shouldHide) }
                AssistChip(
                    onClick = {
                        if (!hidden) {
                            onTagClick(it.id, it.text)
                        } else {
                            hidden = false
                        }
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (hidden) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        } else {
                            it.containerColor
                        }
                    ),
                    leadingIcon = {
                        if (it.leadingIconVector != null
                            && it.leadingIconContentDescription != null
                        ) {
                            Icon(
                                painter = rememberVectorPainter(it.leadingIconVector!!),
                                contentDescription = stringResource(
                                    it.leadingIconContentDescription!!
                                ),
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .size(16.dp)
                            )
                        }
                    },
                    label = {
                        AutoHeightText(
                            text = if (hidden && it.textHiddenRes != null) {
                                stringResource(it.textHiddenRes!!)
                            } else {
                                it.text
                            },
                            color = it.textColor,
                        )
                    },
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }

    interface Entry {
        object Loading : Entry {
            override val image = null
            override val title = ""
            override val tags = emptyList<Tag>()
        }

        val image: String?
        val title: String?

        val subtitleMediaFormatRes: Int get() = R.string.anime_media_format_tv
        val subtitleStatusRes: Int get() = R.string.anime_media_status_finished
        val subtitleSeason: MediaSeason? get() = null
        val subtitleYear: String? get() = "2023"

        val rating: Int? get() = 99
        val popularity: Int? get() = 12345

        val nextAiringEpisode: AniListListRowMedia.NextAiringEpisode? get() = null

        val tags: List<Tag>
    }

    abstract class MediaEntry(
        val media: AniListListRowMedia
    ) : Entry {

        val id = EntryId("item", media.id.toString())
        override val image = media.coverImage?.large
        override val title = media.title?.userPreferred

        override val subtitleMediaFormatRes = media.format.toTextRes()
        override val subtitleStatusRes = media.status.toTextRes()
        override val subtitleSeason = media.season
        override val subtitleYear = media.seasonYear?.toString()

        override val rating = media.averageScore
        override val popularity = media.popularity

        override val nextAiringEpisode = media.nextAiringEpisode

        override val tags: List<Tag> = media.tags?.filterNotNull()?.map {
            object : Tag {
                override val id get() = it.id.toString()

                override val shouldHide = (it.isAdult ?: false)
                        || (it.isGeneralSpoiler ?: false)
                        || (it.isMediaSpoiler ?: false)

                override val containerColor = MediaUtils.calculateTagColor(it.id)

                override val leadingIconVector = MediaUtils.tagLeadingIcon(
                    isAdult = it.isAdult,
                    isGeneralSpoiler = it.isGeneralSpoiler,
                    isMediaSpoiler = it.isMediaSpoiler,
                )

                override val leadingIconContentDescription =
                    MediaUtils.tagLeadingIconContentDescription(
                        isAdult = it.isAdult,
                        isGeneralSpoiler = it.isGeneralSpoiler,
                        isMediaSpoiler = it.isMediaSpoiler,
                    )

                override val textColor = ColorUtils.bestTextColor(containerColor)

                override val text = it.name

                override val textHiddenRes = when {
                    it.isAdult ?: false -> R.string.anime_media_tag_adult
                    (it.isGeneralSpoiler ?: false) || (it.isMediaSpoiler ?: false) ->
                        R.string.anime_media_tag_spoiler
                    else -> null
                }
            }
        }.orEmpty()
    }

    interface Tag {
        val id: String
        val shouldHide: Boolean get() = false
        val containerColor: Color get() = Color.Transparent
        val leadingIconVector: ImageVector? get() = null
        val leadingIconContentDescription: Int? get() = null
        val textColor: Color get() = Color.Unspecified
        val text: String
        val textHiddenRes: Int? get() = null
    }
}

@Preview
@Composable
private fun Preview() {
    AnimeListMediaRow(object : AnimeListMediaRow.Entry {
        override val image = null
        override val title =
            "Tsundere Akuyaku Reijou Liselotte to Jikkyou no Endou-kun to Kaisetsu no Kobayashi-san"
        override val tags: List<AnimeListMediaRow.Tag> = listOf(
            object : AnimeListMediaRow.Tag {
                override val id = "857"
                override val text = "Villainess"
            },
            object : AnimeListMediaRow.Tag {
                override val id = "164"
                override val text = "Tsundere"
            },
            object : AnimeListMediaRow.Tag {
                override val id = "85"
                override val shouldHide = true
                override val leadingIconVector = Icons.Filled.Warning
                override val leadingIconContentDescription = R.string.anime_media_tag_is_spoiler
                override val text = "Tragedy"
                override val textHiddenRes = R.string.anime_media_tag_spoiler
            },
        )
    })
}