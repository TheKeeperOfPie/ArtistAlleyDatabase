package com.thekeeperofpie.artistalleydatabase.anime.forums.thread

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import artistalleydatabase.modules.anime.forums.generated.resources.Res
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_search_error_loading
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_thread_default_title
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_thread_no_comments
import artistalleydatabase.modules.anime.ui.generated.resources.anime_writing_reply_fab_content_description
import com.anilist.data.fragment.MediaNavigationData
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListViewer
import com.thekeeperofpie.artistalleydatabase.anime.forums.ThreadComment
import com.thekeeperofpie.artistalleydatabase.anime.forums.ThreadDeleteCommentPrompt
import com.thekeeperofpie.artistalleydatabase.anime.forums.ThreadHeader
import com.thekeeperofpie.artistalleydatabase.anime.forums.ThreadPageIndicator
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment.ForumCommentEntry
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment.ForumCommentReplyData
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEditBottomSheetScaffoldComposable
import com.thekeeperofpie.artistalleydatabase.anime.ui.UserRoute
import com.thekeeperofpie.artistalleydatabase.anime.ui.WritingReplyPanelScaffold
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.lists.VerticalList
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.PagingPlaceholderContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.PagingPlaceholderKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.utils_compose.showFloatingActionButtonOnVerticalScroll
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ForumThreadScreen {

    @Composable
    operator fun <MediaEntry> invoke(
        mediaEditBottomSheetScaffold: MediaEditBottomSheetScaffoldComposable,
        viewer: () -> AniListViewer?,
        threadId: String,
        upIconOption: UpIconOption?,
        refresh: RefreshFlow, // TODO: Abstract this?
        onRefresh: () -> Unit,
        title: String?,
        state: ForumThreadState<MediaEntry>,
        comments: StateFlow<PagingData<ForumCommentEntry>>,
        userRoute: UserRoute,
        onSendReply: (String) -> Unit,
        onClickReplyComment: (String, MarkdownText?) -> Unit,
        onConfirmDelete: (commentId: String) -> Unit,
        onStatusUpdate: (ForumThreadToggleUpdate) -> Unit,
        onCommentLikeStatusUpdate: (String, Boolean) -> Unit,
        mediaItemKey: (MediaEntry) -> String,
        mediaRow: @Composable (
            MediaEntry?,
            onClickListEdit: (MediaNavigationData) -> Unit,
            Modifier,
        ) -> Unit,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val refresh by refresh.updates.collectAsState()
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
        val sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
        WritingReplyPanelScaffold(
            sheetState = sheetState,
            snackbarHostState = snackbarHostState,
            refreshEvent = refresh,
            committing = state.committing,
            onClickSend = onSendReply,
            topBar = {
                EnterAlwaysTopAppBarHeightChange(scrollBehavior = scrollBehavior) {
                    TopAppBar(
                        title = {
                            Text(
                                text = title ?: entry.result?.thread?.title
                                ?: stringResource(Res.string.anime_forum_thread_default_title),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        navigationIcon = {
                            if (upIconOption != null) {
                                UpIconButton(upIconOption)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                lerp(0.dp, 16.dp, scrollBehavior.state.overlappedFraction)
                            )
                        ),
                    )
                }
            },
            writingPreview = state.replyData?.text,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { writingReplyScaffoldPadding ->
            val scope = rememberCoroutineScope()
            val lazyListState = rememberLazyListState()
            mediaEditBottomSheetScaffold { padding, onClickListEdit ->
                Scaffold(
                    floatingActionButton = {
                        val showFloatingActionButton = viewer() != null &&
                                lazyListState.showFloatingActionButtonOnVerticalScroll()
                        AnimatedVisibility(
                            visible = showFloatingActionButton,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            FloatingActionButton(onClick = {
                                state.replyData = ForumCommentReplyData(null, null)
                                scope.launch { sheetState.expand() }
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Reply,
                                    contentDescription = stringResource(
                                        artistalleydatabase.modules.anime.ui.generated.resources
                                            .Res.string.anime_writing_reply_fab_content_description
                                    ),
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(padding)
                        // Ignore bottom padding so FAB is linked to bottom
                        .padding(PaddingValues(top = writingReplyScaffoldPadding.calculateTopPadding()))
                        .fillMaxSize()
                ) {
                    val comments = comments.collectAsLazyPagingItems()
                    val refreshState = comments.loadState.refresh
                    val refreshing = refreshState is LoadState.Loading
                    val pullRefreshState = rememberPullRefreshState(
                        refreshing = refreshing,
                        onRefresh = onRefresh,
                    )
                    var deletePromptData by remember {
                        mutableStateOf<Pair<String, MarkdownText?>?>(
                            null
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(it)
                            .pullRefresh(pullRefreshState)
                    ) {
                        var maxPage = (comments.itemCount - 1) / 10 + 1
                        var page by rememberSaveable { mutableIntStateOf(1) }
                        var pageString by rememberSaveable { mutableStateOf("1") }
                        LazyColumn(
                            state = lazyListState,
                            contentPadding = PaddingValues(bottom = 100.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val threadEntry = entry.result
                            item("threadHeader") {
                                ThreadHeader(
                                    threadId = threadId,
                                    viewer = viewer(),
                                    entry = threadEntry,
                                    onStatusUpdate = onStatusUpdate,
                                    userRoute = userRoute,
                                )
                            }

                            val media = state.media
                            itemsIndexed(
                                items = media,
                                key = { _, item -> "media-${mediaItemKey(item)}" },
                                contentType = { _, _ -> "media" },
                            ) { index, item ->
                                mediaRow(
                                    item,
                                    onClickListEdit,
                                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                                )
                            }

                            if (comments.itemCount == 0) {
                                if (!refreshing) {
                                    if (refreshState is LoadState.Error) {
                                        item("commentsError") {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                VerticalList.ErrorContent(
                                                    errorText = stringResource(Res.string.anime_forum_search_error_loading),
                                                    exception = refreshState.error,
                                                )
                                            }
                                        }
                                    } else {
                                        item("commentsNoResults") {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.anime_forum_thread_no_comments),
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    modifier = Modifier.padding(
                                                        horizontal = 16.dp,
                                                        vertical = 10.dp
                                                    ),
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                item("pageIndicatorTop") {
                                    Column {
                                        HorizontalDivider()
                                        ThreadPageIndicator(
                                            page = page,
                                            pageString = pageString,
                                            maxPage = maxPage,
                                            onPageChange = { page = it },
                                            onPageStringChange = { pageString = it },
                                        )

                                        HorizontalDivider()
                                    }
                                }

                                // TODO: Recomposition requires this to be set here since API can
                                //  change the max count
                                maxPage = (comments.itemCount - 1) / 10 + 1
                                if (page > maxPage) {
                                    page = maxPage
                                    pageString = page.toString()
                                }
                                val count = if (page == maxPage) {
                                    val remainder = comments.itemCount % 10
                                    if (remainder == 0) 10 else remainder
                                } else {
                                    10.coerceAtMost(comments.itemCount)
                                }
                                val indexPrefix = (page - 1) * 10
                                items(
                                    count = count,
                                    key = {
                                        comments[indexPrefix + it]?.comment?.id
                                            ?: PagingPlaceholderKey(it)
                                    },
                                    contentType = {
                                        comments[indexPrefix + it]?.let { "comment" }
                                            ?: PagingPlaceholderContentType
                                    },
                                ) {
                                    val commentEntry = comments[indexPrefix + it]
                                    ThreadComment(
                                        threadId = threadId,
                                        viewer = viewer(),
                                        entry = commentEntry,
                                        userRoute = userRoute,
                                        onStatusUpdate = onCommentLikeStatusUpdate,
                                        onClickDelete = { commentId, commentMarkdown ->
                                            deletePromptData = commentId to commentMarkdown
                                        },
                                        onClickReplyComment = { commentId, commentMarkdown ->
                                            onClickReplyComment(commentId, commentMarkdown)
                                            scope.launch { sheetState.expand() }
                                        }
                                    )
                                }

                                if (maxPage > 1) {
                                    item("pageIndicatorBottom") {
                                        ThreadPageIndicator(
                                            page = page,
                                            pageString = pageString,
                                            maxPage = maxPage,
                                            onPageChange = { page = it },
                                            onPageStringChange = { pageString = it },
                                        )
                                    }
                                }
                            }
                        }

                        PullRefreshIndicator(
                            refreshing = refreshing,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }

                    val deletePromptDataFinal = deletePromptData
                    if (deletePromptDataFinal != null) {
                        ThreadDeleteCommentPrompt(
                            commentId = deletePromptDataFinal.first,
                            commentMarkdown = deletePromptDataFinal.second,
                            deleting = { state.deleting },
                            onDismiss = { deletePromptData = null },
                            onConfirmDelete = onConfirmDelete,
                        )
                    }
                }
            }
        }
    }
}
