@file:OptIn(ExperimentalFoundationApi::class)

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.fragment.MediaNavigationData
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaFormat
import com.anilist.type.MediaListStatus
import com.anilist.type.MediaType
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaTagEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaGenre
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils.toStatusIcon
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.filter.TagSection
import com.thekeeperofpie.artistalleydatabase.anime.ui.listSection
import com.thekeeperofpie.artistalleydatabase.compose.DetailsSectionHeader
import com.thekeeperofpie.artistalleydatabase.compose.currentLocale
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import com.thekeeperofpie.artistalleydatabase.compose.recomposeHighlighter
import com.thekeeperofpie.artistalleydatabase.utils_compose.fadingEdgeEnd

fun <T> LazyListScope.mediaListSection(
    onClickListEdit: (MediaNavigationData) -> Unit,
    viewer: AniListViewer?,
    @StringRes titleRes: Int,
    values: List<T>,
    valueToEntry: (T) -> AnimeMediaListRow.Entry,
    aboveFold: Int,
    hasMoreValues: Boolean,
    expanded: () -> Boolean = { false },
    onExpandedChange: (Boolean) -> Unit = {},
    label: @Composable() ((T) -> Unit)? = null,
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
) { item, paddingBottom ->
    val entry = valueToEntry(item)
    AnimeMediaListRow(
        entry = entry,
        viewer = viewer,
        modifier = Modifier
            .animateItem()
            .padding(start = 16.dp, end = 16.dp, bottom = paddingBottom),
        label = if (label == null) null else {
            { label(item) }
        },
        onClickListEdit = onClickListEdit
    )
}

fun LazyListScope.mediaHorizontalRow(
    viewer: AniListViewer?,
    editViewModel: MediaEditViewModel,
    @StringRes titleRes: Int,
    entries: LazyPagingItems<out MediaGridCard.Entry>,
    forceListEditIcon: Boolean = false,
    onClickViewAll: () -> Unit,
    @StringRes viewAllContentDescriptionTextRes: Int,
) {
    if (entries.itemCount == 0) return
    item("$titleRes-header") {
        DetailsSectionHeader(
            text = stringResource(titleRes),
            onClickViewAll = onClickViewAll,
            viewAllContentDescriptionTextRes = viewAllContentDescriptionTextRes,
        )
    }

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
                    entry = entry,
                    viewer = viewer,
                    onClickListEdit = editViewModel::initialize,
                    modifier = Modifier.width(120.dp),
                    forceListEditIcon = forceListEditIcon
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
    showPopularity: Boolean = true,
    loading: Boolean = false,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
        modifier = modifier,
    ) {
        if (rating != null || loading) {
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
                val ratingOrDefault = rating ?: 66
                Text(
                    text = ratingOrDefault.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )

                val iconTint = remember(ratingOrDefault) { MediaUtils.ratingColor(ratingOrDefault) }
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

        if (showPopularity && popularity != null || loading) {
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
                val popularityOrDefault = popularity ?: 66
                Text(
                    text = popularityOrDefault.toString(),
                    style = MaterialTheme.typography.labelSmall,
                )

                Icon(
                    imageVector = when {
                        popularityOrDefault > 100000 -> Icons.Filled.PeopleAlt
                        popularityOrDefault > 50000 -> Icons.Outlined.PeopleAlt
                        popularityOrDefault > 10000 -> Icons.Filled.Person
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
    nextAiringEpisode: MediaPreview.NextAiringEpisode,
    episodes: Int?,
    format: MediaFormat?,
    showDate: Boolean = true,
) {
    MediaNextAiringSection(
        airingAtAniListTimestamp = nextAiringEpisode.airingAt,
        episode = nextAiringEpisode.episode,
        episodes = episodes,
        format = format,
        showDate = showDate,
    )
}

@Composable
fun MediaNextAiringSection(
    airingAtAniListTimestamp: Int,
    episode: Int,
    episodes: Int?,
    format: MediaFormat?,
    showDate: Boolean = true,
) {
    val text = MediaUtils.nextAiringSectionText(
        airingAtAniListTimestamp = airingAtAniListTimestamp,
        episode = episode,
        episodes = episodes,
        format = format,
        showDate = showDate,
    )

    Text(
        text = text,
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
    loading: Boolean,
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
    val listState = rememberLazyListState()
    LaunchedEffect(tags) {
        listState.scrollToItem(0, 0)
    }
    LazyRow(
        state = listState,
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
            .recomposeHighlighter()
            .then(modifier)
    ) {
        items(tags, { it.id }) {
            AnimeMediaTagEntry.Chip(
                tag = it,
                onTagClick = onTagClick,
                containerColor = tagContainerColor,
                textColor = tagTextColor,
                textStyle = tagTextStyle,
                modifier = Modifier
                    .height(height)
                    .recomposeHighlighter()
                    .placeholder(
                        visible = loading,
                        shape = RoundedCornerShape(12.dp),
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )
        }
    }
}

@Composable
fun MediaListQuickEditIconButton(
    viewer: AniListViewer?,
    mediaType: MediaType?,
    media: MediaStatusAware,
    maxProgress: Int?,
    maxProgressVolumes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textVerticalPadding: Dp = 4.dp,
    padding: Dp = 8.dp,
    iconSize: Dp = 20.dp,
    forceListEditIcon: Boolean = false,
) {
    MediaListQuickEditIconButton(
        viewer = viewer,
        mediaType = mediaType,
        listStatus = media.mediaListStatus,
        progress = media.progress,
        progressVolumes = media.progressVolumes,
        scoreRaw = media.scoreRaw,
        maxProgress = maxProgress,
        maxProgressVolumes = maxProgressVolumes,
        onClick = onClick,
        modifier = modifier,
        textVerticalPadding = textVerticalPadding,
        padding = padding,
        iconSize = iconSize,
        forceListEditIcon = forceListEditIcon
    )
}

@Composable
fun MediaListQuickEditIconButton(
    viewer: AniListViewer?,
    mediaType: MediaType?,
    listStatus: MediaListStatus?,
    progress: Int?,
    progressVolumes: Int?,
    scoreRaw: Double?,
    maxProgress: Int?,
    maxProgressVolumes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textVerticalPadding: Dp = 4.dp,
    padding: Dp = 8.dp,
    iconSize: Dp = 20.dp,
    forceListEditIcon: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(topEnd = 12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.66f))
            .clickable(onClick = onClick)
            .padding(padding)
    ) {
        val (imageVector, contentDescriptionRes) = if (forceListEditIcon && listStatus == null) {
            Icons.Filled.Edit to
                    R.string.anime_media_details_fab_user_status_edit_icon_content_description
        } else {
            listStatus.toStatusIcon(mediaType)
        }
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(contentDescriptionRes),
            modifier = Modifier.size(iconSize)
        )

        val scoreFormat = viewer?.scoreFormat
        val trailingText = when {
            listStatus == MediaListStatus.CURRENT -> {
                val realProgress = if (mediaType == MediaType.MANGA) progressVolumes else progress
                if (realProgress != null) {
                    val realMaxProgress =
                        if (mediaType == MediaType.MANGA) maxProgressVolumes else maxProgress
                    if (realMaxProgress != null) {
                        stringResource(
                            R.string.anime_media_current_progress,
                            realProgress.toString(),
                            realMaxProgress.toString(),
                        )
                    } else {
                        stringResource(
                            R.string.anime_media_current_progress_unknown_max,
                            realProgress.toString()
                        )
                    }
                } else null
            }
            listStatus == MediaListStatus.COMPLETED && scoreRaw != null && scoreFormat != null -> {
                MediaUtils.scoreFormatToText(
                    LocalConfiguration.currentLocale,
                    scoreRaw,
                    scoreFormat,
                )
            }
            else -> null
        }
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(vertical = textVerticalPadding)
            )
        }
    }
}

@Composable
fun MediaTagPreview(
    tag: TagSection.Tag,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tag.name) },
        text = {
            Column {
                if (tag.category != null) {
                    Text(
                        tag.category,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
                Text(
                    tag.description ?: stringResource(R.string.anime_media_tag_no_description_error)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(UtilsStringR.close))
            }
        }
    )
}

@Composable
fun MediaGenrePreview(
    genre: MediaGenre,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(genre.id) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.anime_media_genre_description_attribution),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                Text(text = stringResource(genre.textRes))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(UtilsStringR.close))
            }
        }
    )
}

@Composable
fun MediaViewOptionRow(
    mediaViewOption: MediaViewOption,
    viewer: AniListViewer?,
    onClickListEdit: (MediaNavigationData) -> Unit,
    entry: MediaPreviewWithDescriptionEntry?,
    forceListEditIcon: Boolean = false,
    showQuickEdit: Boolean = true,
) {
    when (mediaViewOption) {
        MediaViewOption.SMALL_CARD -> AnimeMediaListRow(
            entry = entry,
            viewer = viewer,
            onClickListEdit = onClickListEdit,
            forceListEditIcon = forceListEditIcon,
            showQuickEdit = showQuickEdit,
        )
        MediaViewOption.LARGE_CARD -> AnimeMediaLargeCard(
            viewer = viewer,
            entry = entry,
            forceListEditIcon = forceListEditIcon,
            showQuickEdit = showQuickEdit,
        )
        MediaViewOption.COMPACT -> AnimeMediaCompactListRow(
            viewer = viewer,
            entry = entry,
            onClickListEdit = onClickListEdit,
            forceListEditIcon = forceListEditIcon,
            showQuickEdit = showQuickEdit,
        )
        MediaViewOption.GRID -> MediaGridCard(
            entry = entry,
            viewer = viewer,
            onClickListEdit = onClickListEdit,
            forceListEditIcon = forceListEditIcon,
            showQuickEdit = showQuickEdit,
        )
    }
}
