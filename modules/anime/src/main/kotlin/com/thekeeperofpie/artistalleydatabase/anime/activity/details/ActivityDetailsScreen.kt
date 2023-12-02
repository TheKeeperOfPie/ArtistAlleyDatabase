package com.thekeeperofpie.artistalleydatabase.anime.activity.details

import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.anilist.ActivityDetailsQuery
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ListActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activity.MessageActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activity.TextActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.writing.WritingReplyPanelScaffold
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.PlaceholderHighlight
import com.thekeeperofpie.artistalleydatabase.compose.placeholder.placeholder
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class
)
object ActivityDetailsScreen {

    private val SCREEN_KEY = AnimeNavDestinations.ACTIVITY_DETAILS.id

    @Composable
    operator fun invoke(
        viewModel: ActivityDetailsViewModel = hiltViewModel(),
        upIconOption: UpIconOption.Back,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        val snackbarHostState = remember { SnackbarHostState() }
        val entry = viewModel.entry
        val errorText = entry.error?.let { stringResource(it.first) }
            ?: viewModel.error?.first?.let { stringResource(it) }
        LaunchedEffect(errorText) {
            if (errorText != null) {
                snackbarHostState.showSnackbar(
                    message = errorText,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )
                // TODO: Unified error handling
                viewModel.entry = viewModel.entry.copy(error = null)
                viewModel.error = null
            }
        }

        val editViewModel = hiltViewModel<MediaEditViewModel>()
        MediaEditBottomSheetScaffold(
            screenKey = SCREEN_KEY,
            viewModel = editViewModel,
        ) {
            val refresh by viewModel.refresh.collectAsState()
            val sheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Hidden,
                skipHiddenState = false
            )
            WritingReplyPanelScaffold(
                sheetState = sheetState,
                snackbarHostState = snackbarHostState,
                refreshEvent = refresh,
                committing = viewModel.replying,
                onClickSend = viewModel::sendReply,
                topBar = {
                    AppBar(
                        text = stringResource(R.string.anime_activity_details_title),
                        upIconOption = upIconOption,
                        scrollBehavior = scrollBehavior,
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                val scope = rememberCoroutineScope()
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = { scope.launch { sheetState.expand() } }) {
                            Icon(
                                Icons.Filled.Reply,
                                contentDescription = stringResource(
                                    R.string.anime_writing_reply_fab_content_description
                                ),
                            )
                        }
                    },
                    // Ignore bottom padding so FAB is linked to bottom
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(PaddingValues(top = it.calculateTopPadding())),
                ) {
                    val pullRefreshState = rememberPullRefreshState(
                        refreshing = entry.loading,
                        onRefresh = viewModel::refresh,
                    )
                    Box(
                        modifier = Modifier
                            .padding(it)
                            .pullRefresh(pullRefreshState)
                    ) {
                        val viewer by viewModel.viewer.collectAsState()
                        val replies = viewModel.replies.collectAsLazyPagingItems()
                        var deletePromptData by remember(refresh) {
                            mutableStateOf<Either<Unit, ActivityDetailsViewModel.Entry.ReplyEntry>?>(
                                null
                            )
                        }
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item(viewModel.activityId) {
                                val result = entry.result
                                when (val activity = result?.activity) {
                                    is ActivityDetailsQuery.Data.ListActivityActivity -> {
                                        ListActivitySmallCard(
                                            screenKey = SCREEN_KEY,
                                            viewer = viewer,
                                            activity = activity,
                                            mediaEntry = result.mediaEntry,
                                            entry = result,
                                            onActivityStatusUpdate = viewModel.toggleHelper::toggle,
                                            showActionsRow = true,
                                            onClickDelete = {
                                                deletePromptData = Either.Left(Unit)
                                            },
                                            onClickListEdit = editViewModel::initialize,
                                        )
                                    }
                                    is ActivityDetailsQuery.Data.TextActivityActivity -> {
                                        TextActivitySmallCard(
                                            screenKey = SCREEN_KEY,
                                            activity = activity,
                                            viewer = viewer,
                                            entry = result,
                                            onActivityStatusUpdate = viewModel.toggleHelper::toggle,
                                            showActionsRow = true,
                                            onClickDelete = {
                                                deletePromptData = Either.Left(Unit)
                                            },
                                        )
                                    }
                                    is ActivityDetailsQuery.Data.MessageActivityActivity -> {
                                        MessageActivitySmallCard(
                                            screenKey = SCREEN_KEY,
                                            activity = activity,
                                            viewer = viewer,
                                            entry = result,
                                            onActivityStatusUpdate = viewModel.toggleHelper::toggle,
                                            showActionsRow = true,
                                            onClickDelete = {
                                                deletePromptData = Either.Left(Unit)
                                            },
                                        )
                                    }
                                    is ActivityDetailsQuery.Data.OtherActivity,
                                    null,
                                    -> {
                                        TextActivitySmallCard(
                                            screenKey = SCREEN_KEY,
                                            activity = null,
                                            viewer = viewer,
                                            entry = result,
                                            onActivityStatusUpdate = viewModel.toggleHelper::toggle,
                                        )
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
                                    screenKey = SCREEN_KEY,
                                    viewer = viewer,
                                    replyEntry = replyEntry,
                                    onStatusUpdate = viewModel.replyToggleHelper::toggleLike,
                                    onClickDelete = {
                                        if (replyEntry != null) {
                                            deletePromptData = Either.Right(replyEntry)
                                        }
                                    },
                                )
                            }
                        }

                        val deletePromptDataFinal = deletePromptData
                        if (deletePromptDataFinal != null) {
                            AlertDialog(
                                onDismissRequest = { deletePromptData = null },
                                title = {
                                    Text(
                                        text = stringResource(
                                            if (deletePromptDataFinal is Either.Left) {
                                                R.string.anime_activity_delete_confirmation
                                            } else {
                                                R.string.anime_activity_reply_delete_confirmation
                                            }
                                        )
                                    )
                                },
                                text = if (deletePromptDataFinal is Either.Right) {
                                    {
                                        ImageHtmlText(
                                            text = deletePromptDataFinal.value.reply.text.orEmpty(),
                                            color = MaterialTheme.typography.bodySmall.color,
                                        )
                                    }
                                } else null,
                                confirmButton = {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.height(IntrinsicSize.Min)
                                    ) {
                                        val loadingAlpha by animateFloatAsState(
                                            targetValue = if (viewModel.deleting) 1f else 0f,
                                            label = "Activity deleting crossfade",
                                        )
                                        TextButton(
                                            onClick = { viewModel.delete(deletePromptDataFinal) },
                                            modifier = Modifier.alpha(1f - loadingAlpha)
                                        ) {
                                            Text(text = stringResource(UtilsStringR.delete))
                                        }
                                        CircularProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .alpha(loadingAlpha)
                                        )
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { deletePromptData = null }) {
                                        Text(text = stringResource(UtilsStringR.cancel))
                                    }
                                }
                            )
                        }

                        PullRefreshIndicator(
                            refreshing = entry.loading,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
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
    screenKey: String,
    viewer: AniListViewer?,
    replyEntry: ActivityDetailsViewModel.Entry.ReplyEntry?,
    onStatusUpdate: (String, Boolean) -> Unit,
    onClickDelete: (String) -> Unit,
) {
    val user = replyEntry?.reply?.user
    Column(modifier = Modifier.fillMaxWidth()) {
        val navigationCallback = LocalNavigationCallback.current
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
                UserAvatarImage(
                    screenKey = screenKey,
                    userId = user?.id?.toString(),
                    image = image,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .placeholder(
                            visible = replyEntry == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name ?: "USERNAME",
                    modifier = Modifier.placeholder(
                        visible = replyEntry == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )

                val timestamp = remember(replyEntry) {
                    replyEntry?.reply?.let {
                        DateUtils.getRelativeTimeSpanString(
                            it.createdAt * 1000L,
                            Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond() * 1000,
                            0,
                            DateUtils.FORMAT_ABBREV_ALL,
                        )
                    }
                }

                if (replyEntry == null || timestamp != null) {
                    Text(
                        text = timestamp.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.placeholder(
                            visible = replyEntry == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (viewer != null && viewer.id == user?.id?.toString()) {
                    IconButton(onClick = { onClickDelete(replyEntry.reply.id.toString()) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(
                                R.string.anime_activity_reply_delete_content_description
                            ),
                        )
                    }
                }

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
                            onStatusUpdate(
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
