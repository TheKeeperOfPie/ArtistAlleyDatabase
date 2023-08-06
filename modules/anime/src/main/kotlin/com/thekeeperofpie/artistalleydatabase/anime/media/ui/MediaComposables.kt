@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.thekeeperofpie.artistalleydatabase.anime.media.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.AuthedUserQuery
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toStatusIcon
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.compose.ColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.fadingEdgeEnd

fun <T> LazyListScope.mediaListSection(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    @StringRes titleRes: Int,
    values: Collection<T>,
    valueToEntry: (T) -> AnimeMediaListRow.Entry<*>,
    aboveFold: Int,
    hasMoreValues: Boolean,
    expanded: () -> Boolean = { false },
    onExpandedChange: (Boolean) -> Unit = {},
    colorCalculationState: ColorCalculationState,
    onClickListEdit: (AnimeMediaListRow.Entry<*>) -> Unit,
    onLongClick: (AnimeMediaListRow.Entry<*>) -> Unit,
    label: (@Composable (T) -> Unit)? = null,
    onClickViewAll: ((AnimeNavigator.NavigationCallback) -> Unit)? = null,
    @StringRes viewAllContentDescriptionTextRes: Int? = null,
) = listSection(
    titleRes = titleRes,
    values = values,
    valueToId = { "anime_media_${valueToEntry(it).media.id}" },
    aboveFold = aboveFold,
    hasMoreValues = hasMoreValues,
    expanded = expanded,
    onExpandedChange = onExpandedChange,
    onClickViewAll = onClickViewAll,
    viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
) { item, paddingBottom, modifier ->
    val entry = valueToEntry(item)
    AnimeMediaListRow(
        screenKey = screenKey,
        entry = entry,
        viewer = viewer,
        label = if (label == null) null else {
            { label(item) }
        },
        onClickListEdit = onClickListEdit,
        onLongClick = onLongClick,
        colorCalculationState = colorCalculationState,
        modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = paddingBottom)
    )
}

fun LazyListScope.mediaHorizontalRow(
    screenKey: String,
    viewer: AuthedUserQuery.Data.Viewer?,
    editViewModel: MediaEditViewModel,
    @StringRes titleRes: Int,
    entries: LazyPagingItems<out MediaGridCard.Entry>,
    colorCalculationState: ColorCalculationState,
    sectionTitle: @Composable () -> Unit = {
        DetailsSectionHeader(stringResource(titleRes))
    },
    forceListEditIcon: Boolean = false,
) {
    if (entries.itemCount == 0) return
    item("$titleRes-header") { sectionTitle() }

    item("$titleRes-media") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                count = entries.itemCount,
                key = entries.itemKey { it.media.id },
                contentType = entries.itemContentType { "media" },
            ) {
                val entry = entries[it]
                MediaGridCard(
                    screenKey = screenKey,
                    entry = entry,
                    viewer = viewer,
                    onClickListEdit = { editViewModel.initialize(it.media) },
                    onLongClick = {/* TODO: Ignored */ },
                    colorCalculationState = colorCalculationState,
                    forceListEditIcon = forceListEditIcon,
                    modifier = Modifier.width(120.dp)
                )
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

                val iconTint = remember(rating) { MediaUtils.ratingColor(rating) }
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
fun MediaNextAiringSection(nextAiringEpisode: MediaPreview.NextAiringEpisode) {
    val context = LocalContext.current
    val airingAt = remember(nextAiringEpisode) {
        MediaUtils.formatAiringAt(context, nextAiringEpisode.airingAt * 1000L)
    }

    // TODO: De-dupe airingAt and remainingTime if both show a specific date
    //  (airing > 7 days away)
    val remainingTime = remember(nextAiringEpisode) {
        MediaUtils.formatRemainingTime(nextAiringEpisode.airingAt * 1000L)
    }

    Text(
        text = if (airingAt.contains(remainingTime)) {
            stringResource(
                R.string.anime_media_next_airing_episode,
                nextAiringEpisode.episode,
                airingAt,
            )
        } else {
            stringResource(
                R.string.anime_media_next_airing_episode_with_relative,
                nextAiringEpisode.episode,
                airingAt,
                remainingTime,
            )
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.typography.labelSmall.color
            .takeOrElse { LocalContentColor.current }
            .copy(alpha = 0.8f),
        modifier = Modifier
            .wrapContentHeight(Alignment.Bottom)
            .padding(start = 12.dp, end = 16.dp)
    )
}

@Composable
fun MediaTagRow(
    tags: List<AnimeMediaTagEntry>,
    onTagClick: (tagId: String, tagName: String) -> Unit,
    tagContainerColor: Color,
    tagTextColor: Color,
    modifier: Modifier = Modifier,
    tagTextStyle: TextStyle? = null,
    height: Dp = 24.dp,
    startPadding: Dp = 12.dp,
    bottomPadding: Dp = 10.dp,
) {
    if (tags.isEmpty()) return
    LazyRow(
        contentPadding = PaddingValues(start = startPadding, end = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(top = 4.dp, bottom = bottomPadding)
            .fillMaxWidth()
            // SubcomposeLayout doesn't support fill max width, so use a really large number.
            // The parent will clamp the actual width so all content still fits on screen.
            .size(width = LocalConfiguration.current.screenWidthDp.dp, height = height)
            .fadingEdgeEnd(
                startOpaque = startPadding,
                endOpaque = 32.dp,
                endTransparent = 16.dp,
            )
            .then(modifier)
    ) {
        items(tags, { it.id }) {
            AnimeMediaTagEntry.Chip(
                tag = it,
                onTagClick = onTagClick,
                containerColor = tagContainerColor,
                textColor = tagTextColor,
                textStyle = tagTextStyle,
                modifier = Modifier.height(height),
            )
        }
    }
}

@Composable
fun MediaListQuickEditIconButton(
    mediaType: MediaType?,
    listStatus: MediaListStatus?,
    progress: Int?,
    progressVolumes: Int?,
    maxProgress: Int?,
    maxProgressVolumes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    padding: Dp = 8.dp,
    forceListEditIcon: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(topEnd = 12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.66f))
            .padding(padding)
            .clickable(onClick = onClick)
    ) {
        val (imageVector, contentDescriptionRes) = if (forceListEditIcon) {
            Icons.Filled.Edit to
                    R.string.anime_media_details_fab_user_status_edit_icon_content_description
        } else {
            listStatus.toStatusIcon(mediaType)
        }
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(contentDescriptionRes),
            modifier = Modifier.size(20.dp)
        )

        if (listStatus == MediaListStatus.CURRENT) {
            val realProgress = if (mediaType == MediaType.MANGA) progressVolumes else progress
            if (realProgress != null) {
                val realMaxProgress =
                    if (mediaType == MediaType.MANGA) maxProgressVolumes else maxProgress
                Text(
                    text = if (realMaxProgress != null) stringResource(
                        R.string.anime_media_current_progress,
                        realProgress.toString(),
                        realMaxProgress.toString(),
                    ) else stringResource(
                        R.string.anime_media_current_progress_unknown_max,
                        realProgress.toString()
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
