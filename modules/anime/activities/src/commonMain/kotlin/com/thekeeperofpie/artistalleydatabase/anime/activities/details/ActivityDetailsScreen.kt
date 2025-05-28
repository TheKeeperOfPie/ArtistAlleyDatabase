package com.thekeeperofpie.artistalleydatabase.anime.activities.details

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import artistalleydatabase.modules.anime.activities.generated.resources.Res
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_delete_confirmation
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_details_error_loading_replies
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_details_no_replies
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_details_title
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_reply_delete_confirmation
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_reply_delete_content_description
import artistalleydatabase.modules.anime.activities.generated.resources.anime_activity_reply_like_icon_content_description
import artistalleydatabase.modules.anime.ui.generated.resources.anime_writing_reply_fab_content_description
import artistalleydatabase.modules.utils_compose.generated.resources.cancel
import artistalleydatabase.modules.utils_compose.generated.resources.delete
import com.anilist.data.ActivityDetailsQuery
import com.anilist.data.ActivityDetailsRepliesQuery
import com.anilist.data.fragment.MediaNavigationData
import com.eygraber.compose.placeholder.PlaceholderHighlight
import com.eygraber.compose.placeholder.material3.placeholder
import com.eygraber.compose.placeholder.material3.shimmer
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.activities.ListActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activities.MessageActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activities.TextActivitySmallCard
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.activities.data.ActivityToggleUpdate
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserAvatarImage
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.WritingReplyPanelScaffold
import com.thekeeperofpie.artistalleydatabase.utils.Either
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.AppBar
import com.thekeeperofpie.artistalleydatabase.utils_compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.UtilsStrings
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.SharedTransitionKeyScope
import com.thekeeperofpie.artistalleydatabase.utils_compose.animation.sharedElement
import com.thekeeperofpie.artistalleydatabase.utils_compose.conditionally
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.rememberCoilImageState
import com.thekeeperofpie.artistalleydatabase.utils_compose.image.request
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.LocalNavigationController
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.itemKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import nl.jacobras.humanreadable.HumanReadable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ActivityDetailsScreen {

    @Composable
    operator fun <MediaEntry> invoke(
        upIconOption: UpIconOption.Back,
        refresh: StateFlow<RefreshFlow.Event>,
        viewer: () -> AniListViewer?,
        state: State<MediaEntry>,
        eventSink: (Event) -> Unit,
        userRoute: UserRoute,
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        mediaRow: @Composable (
            MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
    ) {
        mediaEditBottomSheetScaffold { padding, onClickListEdit ->
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            val sheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Hidden,
                skipHiddenState = false
            )
            val snackbarHostState = remember { SnackbarHostState() }
            val entry = state.entry
            val errorText = entry.error?.messageText()
                ?: state.error?.first?.let { stringResource(it) }
            LaunchedEffect(errorText) {
                if (errorText != null) {
                    snackbarHostState.showSnackbar(
                        message = errorText,
                        withDismissAction = true,
                        duration = SnackbarDuration.Long,
                    )
                    // TODO: Unified error handling
                    state.entry = state.entry.copy(error = null)
                    state.error = null
                }
            }
            val refresh by refresh.collectAsState()
            WritingReplyPanelScaffold(
                sheetState = sheetState,
                snackbarHostState = snackbarHostState,
                refreshEvent = refresh,
                committing = state.replying,
                onClickSend = { eventSink(Event.SendReply(it)) },
                topBar = {
                    AppBar(
                        text = stringResource(Res.string.anime_activity_details_title),
                        upIconOption = upIconOption,
                        scrollBehavior = scrollBehavior,
                    )
                },
                modifier = Modifier
                    .padding(padding)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                val scope = rememberCoroutineScope()
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = { scope.launch { sheetState.expand() } }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Reply,
                                contentDescription = stringResource(
                                    artistalleydatabase.modules.anime.ui.generated.resources.Res
                                        .string.anime_writing_reply_fab_content_description
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
                        onRefresh = { eventSink(Event.Refresh) },
                    )
                    Box(
                        modifier = Modifier
                            .padding(it)
                            .pullRefresh(pullRefreshState)
                    ) {
                        val viewer = viewer()
                        val replies = state.replies.collectAsLazyPagingItems()
                        var deletePromptData by remember(refresh) {
                            mutableStateOf<Either<Unit, ReplyEntry>?>(null)
                        }
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 144.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item(state.activityId) {
                                val result = entry.result
                                when (val activity = result?.activity) {
                                    is ActivityDetailsQuery.Data.ListActivityActivity -> {
                                        ListActivitySmallCard<MediaEntry>(
                                            viewer = viewer,
                                            activity = activity,
                                            mediaEntry = result.mediaEntry,
                                            mediaRow = { entry, modifier ->
                                                mediaRow(entry, onClickListEdit, modifier)
                                            },
                                            entry = result,
                                            onActivityStatusUpdate = {
                                                eventSink(Event.ActivityStatusChange(it))
                                            },
                                            showActionsRow = true,
                                            onClickDelete = {
                                                deletePromptData = Either.Left(Unit)
                                            },
                                            userRoute = userRoute,
                                        )
                                    }
                                    is ActivityDetailsQuery.Data.TextActivityActivity -> {
                                        TextActivitySmallCard(
                                            viewer = viewer,
                                            activity = activity,
                                            entry = result,
                                            onActivityStatusUpdate = {
                                                eventSink(Event.ActivityStatusChange(it))
                                            },
                                            showActionsRow = true,
                                            onClickDelete = {
                                                deletePromptData = Either.Left(Unit)
                                            },
                                            userRoute = userRoute,
                                        )
                                    }
                                    is ActivityDetailsQuery.Data.MessageActivityActivity -> {
                                        MessageActivitySmallCard(
                                            viewer = viewer,
                                            activity = activity,
                                            entry = result,
                                            onActivityStatusUpdate = {
                                                eventSink(Event.ActivityStatusChange(it))
                                            },
                                            showActionsRow = true,
                                            onClickDelete = {
                                                deletePromptData = Either.Left(Unit)
                                            },
                                            userRoute = userRoute,
                                        )
                                    }
                                    is ActivityDetailsQuery.Data.OtherActivity,
                                    null,
                                        -> {
                                        TextActivitySmallCard(
                                            viewer = viewer,
                                            activity = null,
                                            entry = result,
                                            onActivityStatusUpdate = {
                                                eventSink(Event.ActivityStatusChange(it))
                                            },
                                            userRoute = userRoute,
                                        )
                                    }
                                }
                            }

                            // TODO: Collapse the error UI with other screens
                            if (replies.itemCount == 0) {
                                when (replies.loadState.refresh) {
                                    is LoadState.Error ->
                                        centeredMessage(Res.string.anime_activity_details_error_loading_replies)
                                    is LoadState.NotLoading ->
                                        centeredMessage(Res.string.anime_activity_details_no_replies)
                                    LoadState.Loading -> Unit
                                }
                            }

                            items(
                                count = replies.itemCount,
                                key = replies.itemKey { it.reply.id },
                                contentType = replies.itemContentType { "reply" },
                            ) {
                                val replyEntry = replies[it]
                                SharedTransitionKeyScope(
                                    "activity_reply",
                                    replyEntry?.reply?.id.toString(),
                                ) {
                                    ReplyRow(
                                        viewer = viewer,
                                        replyEntry = replyEntry,
                                        onStatusUpdate = { activityReplyId, liked ->
                                            eventSink(
                                                Event.ReplyStatusUpdate(
                                                    activityReplyId,
                                                    liked
                                                )
                                            )
                                        },
                                        onClickDelete = {
                                            if (replyEntry != null) {
                                                deletePromptData = Either.Right(replyEntry)
                                            }
                                        },
                                        userRoute = userRoute,
                                    )
                                }
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
                                                Res.string.anime_activity_delete_confirmation
                                            } else {
                                                Res.string.anime_activity_reply_delete_confirmation
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
                                            targetValue = if (state.deleting) 1f else 0f,
                                            label = "Activity deleting crossfade",
                                        )
                                        TextButton(
                                            onClick = {
                                                eventSink(Event.Delete(deletePromptDataFinal))
                                            },
                                            modifier = Modifier.alpha(1f - loadingAlpha)
                                        ) {
                                            Text(
                                                text = stringResource(UtilsStrings.delete)
                                            )
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
                                        Text(text = stringResource(UtilsStrings.cancel))
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


    private fun LazyListScope.centeredMessage(textRes: StringResource) {
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
        viewer: AniListViewer?,
        replyEntry: ReplyEntry?,
        onStatusUpdate: (activityReplyId: String, liked: Boolean) -> Unit,
        onClickDelete: (String) -> Unit,
        userRoute: UserRoute,
    ) {
        val user = replyEntry?.reply?.user
        Column(modifier = Modifier.fillMaxWidth()) {
            val imageState = rememberCoilImageState(user?.avatar?.large)
            val sharedTransitionKey = user?.id?.toString()
                ?.let { SharedTransitionKey.makeKeyForId(it) }
            val navigationController = LocalNavigationController.current
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .clickable {
                        if (user != null) {
                            navigationController.navigate(
                                userRoute(
                                    user.id.toString(),
                                    sharedTransitionKey,
                                    user.name,
                                    imageState.toImageState(),
                                )
                            )
                        }
                    }
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp)
            ) {
                val image = user?.avatar?.large
                if (replyEntry == null || image != null) {
                    UserAvatarImage(
                        imageState = imageState,
                        image = imageState.request().build(),
                        modifier = Modifier
                            .size(32.dp)
                            .sharedElement(sharedTransitionKey, "user_image")
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
                            HumanReadable.timeAgo(Instant.fromEpochSeconds(it.createdAt.toLong()))
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
                                    Res.string.anime_activity_reply_delete_content_description
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
                                Res.string.anime_activity_reply_like_icon_content_description
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

            HorizontalDivider()
        }
    }

    @Stable
    class State<MediaEntry>(val activityId: String) {
        var replying by mutableStateOf(false)
        var deleting by mutableStateOf(false)
        var error by mutableStateOf<Pair<StringResource, Throwable?>?>(null)

        var entry by mutableStateOf<LoadingResult<Entry<MediaEntry>>>(LoadingResult.loading())
        var replies = MutableStateFlow(PagingData.empty<ReplyEntry>())
    }

    sealed interface Event {
        data class ActivityStatusChange(val update: ActivityToggleUpdate) : Event
        data class Delete(val promptData: Either<Unit, ReplyEntry>) : Event
        data class ReplyStatusUpdate(val activityReplyId: String, val liked: Boolean) : Event
        data class SendReply(val reply: String) : Event
        data object Refresh : Event
    }

    data class Entry<MediaEntry>(
        val activity: ActivityDetailsQuery.Data.Activity,
        val mediaEntry: MediaEntry?,
        override val liked: Boolean,
        override val subscribed: Boolean,
    ) : ActivityStatusAware

    data class ReplyEntry(
        val reply: ActivityDetailsRepliesQuery.Data.Page.ActivityReply,
        val liked: Boolean,
    )
}
