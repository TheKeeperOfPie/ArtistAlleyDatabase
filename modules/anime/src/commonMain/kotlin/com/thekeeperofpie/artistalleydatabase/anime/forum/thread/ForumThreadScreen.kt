package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

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
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_forum_search_error_loading
import artistalleydatabase.modules.anime.generated.resources.anime_forum_thread_default_title
import artistalleydatabase.modules.anime.generated.resources.anime_forum_thread_no_comments
import artistalleydatabase.modules.anime.generated.resources.anime_writing_reply_fab_content_description
import com.thekeeperofpie.artistalleydatabase.anime.AnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.LocalAnimeComponent
import com.thekeeperofpie.artistalleydatabase.anime.forum.ThreadComment
import com.thekeeperofpie.artistalleydatabase.anime.forum.ThreadDeleteCommentPrompt
import com.thekeeperofpie.artistalleydatabase.anime.forum.ThreadHeader
import com.thekeeperofpie.artistalleydatabase.anime.forum.ThreadPageIndicator
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.writing.WritingReplyPanelScaffold
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils_compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.utils_compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.PagingPlaceholderContentType
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.PagingPlaceholderKey
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.collectAsLazyPagingItems
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.PullRefreshIndicator
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.pullRefresh
import com.thekeeperofpie.artistalleydatabase.utils_compose.pullrefresh.rememberPullRefreshState
import com.thekeeperofpie.artistalleydatabase.utils_compose.showFloatingActionButtonOnVerticalScroll
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
object ForumThreadScreen {

    @Composable
    operator fun invoke(
        animeComponent: AnimeComponent = LocalAnimeComponent.current,
        viewModel: ForumThreadViewModel = viewModel {
            animeComponent.forumThreadViewModel(createSavedStateHandle())
        },
        upIconOption: UpIconOption?,
        title: String?,
    ) {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val refresh by viewModel.refresh.collectAsState()
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
        val sheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
        WritingReplyPanelScaffold(
            sheetState = sheetState,
            snackbarHostState = snackbarHostState,
            refreshEvent = refresh,
            committing = viewModel.committing,
            onClickSend = viewModel::sendReply,
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
            writingPreview = viewModel.replyData?.text,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            val scope = rememberCoroutineScope()
            val lazyListState = rememberLazyListState()
            val editViewModel = viewModel { animeComponent.mediaEditViewModel() }
            val viewer by viewModel.viewer.collectAsState()
            MediaEditBottomSheetScaffold(
                modifier = Modifier.padding(PaddingValues(top = it.calculateTopPadding())),
                viewModel = editViewModel,
            ) {
                Scaffold(
                    floatingActionButton = {
                        val showFloatingActionButton = viewer != null &&
                                lazyListState.showFloatingActionButtonOnVerticalScroll()
                        AnimatedVisibility(
                            visible = showFloatingActionButton,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            FloatingActionButton(onClick = {
                                viewModel.replyData = ForumThreadViewModel.ReplyData(null, null)
                                scope.launch { sheetState.expand() }
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Reply,
                                    contentDescription = stringResource(
                                        Res.string.anime_writing_reply_fab_content_description
                                    ),
                                )
                            }
                        }
                    },
                    // Ignore bottom padding so FAB is linked to bottom
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val comments = viewModel.comments.collectAsLazyPagingItems()
                    val refreshState = comments.loadState.refresh
                    val refreshing = refreshState is LoadState.Loading
                    val pullRefreshState = rememberPullRefreshState(
                        refreshing = refreshing,
                        onRefresh = comments::refresh,
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
                                    threadId = viewModel.threadId,
                                    viewer = viewer,
                                    entry = threadEntry,
                                    onStatusUpdate = viewModel.threadToggleHelper::toggle,
                                )
                            }

                            val media = viewModel.media
                            itemsIndexed(
                                items = media,
                                key = { _, item -> "media-${item.media.id}" },
                                contentType = { _, _ -> "media" },
                            ) { index, item ->
                                AnimeMediaCompactListRow(
                                    viewer = viewer,
                                    entry = item,
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 8.dp,
                                    ),
                                    onClickListEdit = editViewModel::initialize
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
                                                AnimeMediaListScreen.ErrorContent(
                                                    errorTextRes = Res.string.anime_forum_search_error_loading,
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
                                        threadId = viewModel.threadId,
                                        viewer = viewer,
                                        entry = commentEntry,
                                        onStatusUpdate = viewModel.commentToggleHelper::toggleLike,
                                        onClickDelete = { commentId, commentMarkdown ->
                                            deletePromptData = commentId to commentMarkdown
                                        },
                                        onClickReplyComment = { commentId, commentMarkdown ->
                                            viewModel.onClickReplyComment(
                                                commentId,
                                                commentMarkdown
                                            )
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
                            deleting = { viewModel.deleting },
                            onDismiss = { deletePromptData = null },
                            onConfirmDelete = {
                                viewModel.deleteComment(deletePromptDataFinal.first)
                            },
                        )
                    }
                }
            }
        }
    }
}
