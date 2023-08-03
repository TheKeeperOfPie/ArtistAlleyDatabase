package com.thekeeperofpie.artistalleydatabase.anime.activity.details

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.material3.BottomSheetScaffold
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.thekeeperofpie.artistalleydatabase.android_utils.Either
import com.thekeeperofpie.artistalleydatabase.android_utils.UtilsStringR
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavigator
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.activity.ListActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activity.MessageActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activity.TextActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class
)
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
        val bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
        val scope = rememberCoroutineScope()
        val refresh by viewModel.refresh.collectAsState()
        var replyValue by rememberSaveable(refresh) { mutableStateOf("") }
        val replying = viewModel.replying
        val keyboardController = LocalSoftwareKeyboardController.current
        LaunchedEffect(replying) {
            if (!replying) {
                keyboardController?.hide()
                bottomSheetState.hide()
            }
        }

        BackHandler(
            enabled = bottomSheetState.targetValue != SheetValue.Hidden
                    && !WindowInsets.isImeVisible
        ) {
            scope.launch { bottomSheetState.hide() }
        }

        val bottomSheetScaffoldState =
            rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
        val entry = viewModel.entry
        val errorText = entry.error?.let { stringResource(it.first) }
        LaunchedEffect(errorText) {
            if (errorText != null) {
                bottomSheetScaffoldState.snackbarHostState.showSnackbar(
                    message = errorText,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )
                viewModel.entry = viewModel.entry.copy(error = null)
            }
        }

        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetContent = {
                ReplySheetContent(
                    replying = { viewModel.replying },
                    value = { replyValue },
                    onValueChange = { replyValue = it },
                    onClickSend = { viewModel.sendReply(replyValue) },
                )
            },
            topBar = {
                AppBar(
                    text = stringResource(R.string.anime_activity_details_title),
                    upIconOption = upIconOption,
                    scrollBehavior = scrollBehavior,
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Box {
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = entry.loading,
                    onRefresh = viewModel::refresh,
                )
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = { scope.launch { bottomSheetState.expand() } }) {
                            Icon(
                                Icons.Filled.Reply,
                                contentDescription = stringResource(
                                    R.string.anime_activity_reply_fab_content_description
                                ),
                            )
                        }
                    },
                    // Ignore bottom padding so FAB is linked to bottom
                    modifier = Modifier
                        .padding(PaddingValues(top = it.calculateTopPadding()))
                        .pullRefresh(pullRefreshState)
                ) {
                    val viewer by viewModel.viewer.collectAsState()
                    val replies = viewModel.replies.collectAsLazyPagingItems()
                    var deletePromptData by remember(refresh) {
                        mutableStateOf<Either<Unit, ActivityDetailsViewModel.Entry.ReplyEntry>?>(
                            null
                        )
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                    ) {
                        item(viewModel.activityId) {
                            val result = entry.result
                            when (val activity = result?.activity) {
                                is ActivityDetailsQuery.Data.ListActivityActivity -> {
                                    ListActivitySmallCard(
                                        screenKey = SCREEN_KEY,
                                        viewer = viewer,
                                        activity = activity,
                                        mediaEntry = result.mediaEntry?.rowEntry,
                                        entry = result,
                                        onActivityStatusUpdate = viewModel.toggleHelper::toggle,
                                        showActionsRow = true,
                                        onClickDelete = { deletePromptData = Either.Left(Unit) },
                                        colorCalculationState = colorCalculationState,
                                        navigationCallback = navigationCallback,
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
                                        onClickDelete = { deletePromptData = Either.Left(Unit) },
                                        navigationCallback = navigationCallback,
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
                                        onClickDelete = { deletePromptData = Either.Left(Unit) },
                                        navigationCallback = navigationCallback,
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
                                        navigationCallback = navigationCallback,
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
                                viewer = viewer,
                                replyEntry = replyEntry,
                                onReplyStatusUpdate = viewModel.replyToggleHelper::toggleLike,
                                onClickDelete = {
                                    if (replyEntry != null) {
                                        deletePromptData = Either.Right(replyEntry)
                                    }
                                },
                                navigationCallback = navigationCallback,
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
                                        color = MaterialTheme.typography.bodySmall.color
                                            .takeOrElse { LocalContentColor.current },
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
                }

                PullRefreshIndicator(
                    refreshing = entry.loading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    @Composable
    private fun ReplySheetContent(
        replying: () -> Boolean,
        value: () -> String,
        onValueChange: (String) -> Unit,
        onClickSend: () -> Unit,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = value(),
                onValueChange = onValueChange,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
            Divider()
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(IntrinsicSize.Min)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val replyingAlpha by animateFloatAsState(
                        targetValue = if (replying()) 1f else 0f,
                        label = "Activity replying crossfade",
                    )
                    TextButton(
                        onClick = {
                            if (value().isNotBlank()) {
                                onClickSend()
                            }
                        },
                        modifier = Modifier.alpha(1f - replyingAlpha)
                    ) {
                        Text(text = stringResource(R.string.anime_activity_reply_send_button))
                    }
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxHeight()
                            .alpha(replyingAlpha)
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
        onClickDelete: (String) -> Unit,
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
                    if (viewer != null && viewer.id == replyEntry?.reply?.user?.id) {
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
