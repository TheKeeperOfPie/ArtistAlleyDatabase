package com.thekeeperofpie.artistalleydatabase.anime.activity.details

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.anilist.ActivityDetailsQuery
import com.anilist.AuthedUserQuery
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ListActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activity.TextActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState

@OptIn(ExperimentalMaterial3Api::class)
object ActivityDetailsScreen {

    private val SCREEN_KEY = AnimeNavDestinations.ACTIVITY_DETAILS.id

    @Composable
    operator fun invoke(
        upIconOption: UpIconOption.Back,
        viewModel: ActivityDetailsViewModel,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        Scaffold(
            topBar = {
                AppBar(
                    text = stringResource(R.string.anime_activity_details_title),
                    upIconOption = upIconOption,
                    scrollBehavior = scrollBehavior,
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            val viewer by viewModel.viewer.collectAsState()
            val replies = viewModel.replies.collectAsLazyPagingItems()
            LazyColumn(modifier = Modifier.padding(it)) {
                item(viewModel.activityId) {
                    val entry = viewModel.entry
                    when (val activity = entry?.activity) {
                        is ActivityDetailsQuery.Data.ListActivityActivity -> {
                            ListActivitySmallCard(
                                screenKey = SCREEN_KEY,
                                viewer = viewer,
                                activity = activity,
                                media = entry.mediaEntry?.media,
                                entry = entry,
                                onActivityStatusUpdate = viewModel.toggleHelper::toggle,
                                colorCalculationState = colorCalculationState,
                                navigationCallback = navigationCallback,
                            )
                        }
                        is ActivityDetailsQuery.Data.MessageActivityActivity -> TODO()
                        is ActivityDetailsQuery.Data.TextActivityActivity -> {
                            TextActivitySmallCard(
                                activity = activity,
                                viewer = viewer,
                                entry = entry,
                                onActivityStatusUpdate = viewModel.toggleHelper::toggle,
                                navigationCallback = navigationCallback,
                            )
                        }
                        is ActivityDetailsQuery.Data.OtherActivity,
                        null,
                        -> {
                            // TODO: Error and loading states
                        }
                    }
                }

                // TODO: Collapse the error UI with other screens
                if (replies.itemCount == 0) {
                    when (replies.loadState.refresh) {
                        is LoadState.Error ->
                            centeredMessage(R.string.anime_activity_details_error_loading_replies)
                        is LoadState.NotLoading ->
                            centeredMessage(R.string.anime_activity_details_no_replies)
                        LoadState.Loading -> Unit
                    }
                }

                items(
                    count = replies.itemCount,
                    key = replies.itemKey { it.reply.id },
                    contentType = replies.itemContentType { "reply" },
                ) {
                    val replyEntry = replies[it]
                    ReplyRow(
                        viewer = viewer,
                        replyEntry = replyEntry,
                        onReplyStatusUpdate = viewModel.replyToggleHelper::toggleLike,
                        navigationCallback = navigationCallback,
                    )
                }
            }
        }
    }

    private fun LazyListScope.centeredMessage(@StringRes textRes: Int) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(textRes)
                )
            }
        }
    }

    @Composable
    private fun ReplyRow(
        viewer: AuthedUserQuery.Data.Viewer?,
        replyEntry: ActivityDetailsViewModel.Entry.ReplyEntry?,
        onReplyStatusUpdate: (String, Boolean) -> Unit,
        navigationCallback: AnimeNavigator.NavigationCallback,
    ) {
        val user = replyEntry?.reply?.user
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .clickable {
                        if (user != null) {
                            navigationCallback.onUserClick(user, 1f)
                        }
                    }
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp)
            ) {
                val image = user?.avatar?.large
                if (replyEntry == null || image != null) {
                    AsyncImage(
                        model = image,
                        contentDescription = stringResource(R.string.anime_user_image),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .placeholder(
                                visible = replyEntry == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                }

                Text(
                    text = user?.name ?: "USERNAME",
                    modifier = Modifier
                        .weight(1f)
                        .placeholder(
                            visible = replyEntry == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val likeCount = replyEntry?.reply?.likeCount ?: 0
                    if (replyEntry == null || likeCount > 0) {
                        Text(
                            text = likeCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .placeholder(
                                    visible = replyEntry == null,
                                    highlight = PlaceholderHighlight.shimmer(),
                                )
                        )
                    }
                    IconButton(
                        enabled = viewer != null,
                        onClick = {
                            if (replyEntry != null) {
                                onReplyStatusUpdate(
                                    replyEntry.reply.id.toString(),
                                    !replyEntry.liked,
                                )
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (replyEntry?.liked == true) {
                                Icons.Filled.ThumbUp
                            } else {
                                Icons.Outlined.ThumbUp
                            },
                            contentDescription = stringResource(
                                R.string.anime_activity_reply_like_icon_content_description
                            ),
                        )
                    }
                }
            }

            ImageHtmlText(
                text = replyEntry?.reply?.text.orEmpty(),
                color = MaterialTheme.typography.bodySmall.color
                    .takeOrElse { LocalContentColor.current },
                modifier = Modifier
                    .conditionally(replyEntry == null) { fillMaxWidth() }
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                    .placeholder(
                        visible = replyEntry == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )

            Divider()
        }
    }
}
