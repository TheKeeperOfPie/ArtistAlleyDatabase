package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

import android.text.Spanned
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.anilist.AuthedUserQuery
import com.anilist.fragment.UserNavigationData
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.thekeeperofpie.artistalleydatabase.android_utils.UriUtils
import com.thekeeperofpie.artistalleydatabase.anilist.AniListUtils
import com.thekeeperofpie.artistalleydatabase.anime.AnimeNavDestinations
import com.thekeeperofpie.artistalleydatabase.anime.LocalNavigationCallback
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.forum.ThreadAuthor
import com.thekeeperofpie.artistalleydatabase.anime.forum.ThreadCategoryRow
import com.thekeeperofpie.artistalleydatabase.anime.forum.ThreadCommentTimestamp
import com.thekeeperofpie.artistalleydatabase.anime.forum.ThreadViewReplyCountIcons
import com.thekeeperofpie.artistalleydatabase.anime.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditBottomSheetScaffold
import com.thekeeperofpie.artistalleydatabase.anime.media.edit.MediaEditViewModel
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.PagingPlaceholderContentType
import com.thekeeperofpie.artistalleydatabase.anime.utils.PagingPlaceholderKey
import com.thekeeperofpie.artistalleydatabase.anime.writing.WritingReplyPanelScaffold
import com.thekeeperofpie.artistalleydatabase.compose.EnterAlwaysTopAppBarHeightChange
import com.thekeeperofpie.artistalleydatabase.compose.MinWidthTextField
import com.thekeeperofpie.artistalleydatabase.compose.UpIconButton
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.openForceExternal
import com.thekeeperofpie.artistalleydatabase.compose.rememberColorCalculationState
import com.thekeeperofpie.artistalleydatabase.compose.showFloatingActionButtonOnVerticalScroll
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class
)
object ForumThreadScreen {

    private val SCREEN_KEY = AnimeNavDestinations.FORUM_THREAD.id

    @Composable
    operator fun invoke(
        viewModel: ForumThreadViewModel = hiltViewModel(),
        upIconOption: UpIconOption?,
        title: String?,
    ) {
        val colorCalculationState = rememberColorCalculationState(viewModel.colorMap)
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
                                text = title
                                    ?: stringResource(R.string.anime_forum_thread_default_title),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        navigationIcon = {
                            if (upIconOption != null) {
                                UpIconButton(upIconOption)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
            val editViewModel = hiltViewModel<MediaEditViewModel>()
            val viewer by viewModel.viewer.collectAsState()
            MediaEditBottomSheetScaffold(
                screenKey = SCREEN_KEY,
                viewModel = editViewModel,
                colorCalculationState = colorCalculationState,
                modifier = Modifier.padding(PaddingValues(top = it.calculateTopPadding())),
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
                                    Icons.Filled.Reply,
                                    contentDescription = stringResource(
                                        R.string.anime_writing_reply_fab_content_description
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
                    var deletePromptData by remember { mutableStateOf<Pair<String, Spanned?>?>(null) }

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
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val threadEntry = entry.result
                            item("threadHeader") {
                                ThreadHeader(
                                    viewModel = viewModel,
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
                                    screenKey = SCREEN_KEY,
                                    viewer = viewer,
                                    entry = item,
                                    onLongClick = {
                                        viewModel.ignoreList.toggle(it.media.id.toString())
                                    },
                                    onClickListEdit = { editViewModel.initialize(it.media) },
                                    colorCalculationState = colorCalculationState,
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 8.dp,
                                    )
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
                                                    errorTextRes = R.string.anime_forum_search_error_loading,
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
                                                    text = stringResource(id = R.string.anime_forum_thread_no_comments),
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
                                        PageIndicator(
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
                                        viewModel = viewModel,
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
                                        PageIndicator(
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
                        AlertDialog(
                            onDismissRequest = { deletePromptData = null },
                            title = {
                                Text(
                                    text = stringResource(
                                        R.string.anime_forum_thread_comment_delete_confirmation
                                    )
                                )
                            },
                            text = {
                                MarkdownText(
                                    text = deletePromptDataFinal.second,
                                    textColor = MaterialTheme.typography.bodySmall.color,
                                )
                            },
                            confirmButton = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.height(IntrinsicSize.Min)
                                ) {
                                    val loadingAlpha by animateFloatAsState(
                                        targetValue = if (viewModel.deleting) 1f else 0f,
                                        label = "Forum thread comment deleting crossfade",
                                    )
                                    TextButton(
                                        onClick = {
                                            viewModel.deleteComment(deletePromptDataFinal.first)
                                        },
                                        modifier = Modifier.alpha(1f - loadingAlpha)
                                    ) {
                                        Text(
                                            text = stringResource(
                                                R.string.anime_forum_thread_comment_delete
                                            )
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
                                    Text(
                                        text = stringResource(
                                            R.string.anime_forum_thread_comment_cancel
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ThreadHeader(
        viewModel: ForumThreadViewModel,
        viewer: AuthedUserQuery.Data.Viewer?,
        entry: ForumThreadViewModel.ThreadEntry?,
        onStatusUpdate: (ForumThreadToggleUpdate) -> Unit,
    ) {
        val thread = entry?.thread
        Column {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = thread?.title ?: "Placeholder title",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .placeholder(
                                visible = entry == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )

                    ThreadAuthor(
                        screenKey = SCREEN_KEY,
                        loading = entry == null,
                        user = thread?.user,
                        aniListTimestamp = thread?.createdAt,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    ThreadViewReplyCountIcons(
                        viewCount = thread?.viewCount,
                        replyCount = thread?.replyCount,
                        modifier = Modifier.padding(top = 12.dp, end = 12.dp)
                    )

                    Box {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(
                                    R.string.anime_forum_thread_more_actions_content_description,
                                ),
                            )
                        }

                        val uriHandler = LocalUriHandler.current
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.anime_forum_thread_open_in_browser)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.OpenInBrowser,
                                        contentDescription = stringResource(
                                            R.string.anime_forum_thread_open_in_browser_content_description
                                        )
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    uriHandler.openForceExternal(
                                        AniListUtils.forumThreadUrl(viewModel.threadId)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (thread != null) {
                ThreadCategoryRow(thread)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (thread == null || !thread.body.isNullOrBlank()) {
                var bodyShown by rememberSaveable(thread?.title) {
                    val mayHaveSpoilers =
                        thread?.title?.contains("Spoilers", ignoreCase = true) == true
                    mutableStateOf(!mayHaveSpoilers)
                }
                if (bodyShown) {
                    MarkdownText(
                        text = entry?.bodyMarkdown,
                        textColor = MaterialTheme.typography.bodySmall.color,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .placeholder(
                                visible = entry == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                } else {
                    Button(
                        onClick = { bodyShown = true },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = stringResource(R.string.anime_forum_thread_show_body_spoilers))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (thread?.user != null && viewer?.id == thread.user?.id) {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(
                                R.string.anime_forum_thread_delete_content_description
                            ),
                        )
                    }
                }

                if (viewer != null) {
                    val liked = entry?.liked ?: false
                    val subscribed = entry?.subscribed ?: false
                    IconButton(
                        onClick = {
                            if (thread != null) {
                                onStatusUpdate(
                                    ForumThreadToggleUpdate.Liked(
                                        id = thread.id.toString(),
                                        liked = !liked,
                                        subscribed = subscribed,
                                    )
                                )
                            }
                        }, modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                            contentDescription = stringResource(
                                R.string.anime_forum_thread_like_icon_content_description
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (thread != null) {
                                onStatusUpdate(
                                    ForumThreadToggleUpdate.Subscribe(
                                        id = thread.id.toString(),
                                        liked = liked,
                                        subscribed = !subscribed,
                                    )
                                )
                            }
                        }, modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (subscribed) {
                                Icons.Filled.NotificationsActive
                            } else {
                                Icons.Filled.NotificationsNone
                            },
                            contentDescription = stringResource(
                                R.string.anime_forum_thread_subscribe_icon_content_description
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                val uriHandler = LocalUriHandler.current
                IconButton(onClick = {
                    if (thread != null) {
                        uriHandler.openUri(
                            AniListUtils.forumThreadUrl(thread.id.toString()) +
                                    "?${UriUtils.FORCE_EXTERNAL_URI_PARAM}=true"
                        )
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.OpenInBrowser,
                        contentDescription = stringResource(
                            R.string.anime_forum_thread_open_in_browser_content_description
                        ),
                    )
                }
            }
        }
    }

    @Composable
    private fun ThreadComment(
        viewModel: ForumThreadViewModel,
        viewer: AuthedUserQuery.Data.Viewer?,
        entry: ForumThreadViewModel.CommentEntry?,
        onStatusUpdate: (String, Boolean) -> Unit,
        onClickDelete: (String, Spanned?) -> Unit,
        onClickReplyComment: (String, Spanned?) -> Unit,
    ) {
        // TODO: Child comments

        val comment = entry?.comment
        Column(modifier = Modifier.fillMaxWidth()) {
            ThreadCommentContent(
                viewModel = viewModel,
                viewer = viewer,
                loading = entry == null,
                commentId = comment?.id?.toString(),
                commentMarkdown = entry?.commentMarkdown,
                createdAt = comment?.createdAt,
                liked = entry?.liked ?: false,
                likeCount = comment?.likeCount ?: 0,
                user = entry?.user,
                onStatusUpdate = onStatusUpdate,
                onClickDelete = onClickDelete,
                onClickReplyComment = onClickReplyComment,
            )

            val children = entry?.children
            if (!children.isNullOrEmpty()) {
                children.forEach {
                    ThreadCommentChild(
                        viewModel = viewModel,
                        viewer = viewer,
                        level = 1,
                        child = it,
                        onStatusUpdate = onStatusUpdate,
                        onClickDelete = onClickDelete,
                        onClickReplyComment = onClickReplyComment,
                    )
                }
            }

            HorizontalDivider()
        }
    }

    @Composable
    private fun ThreadCommentContent(
        viewModel: ForumThreadViewModel,
        viewer: AuthedUserQuery.Data.Viewer?,
        loading: Boolean,
        commentId: String?,
        commentMarkdown: Spanned?,
        createdAt: Int?,
        liked: Boolean,
        likeCount: Int,
        user: UserNavigationData?,
        onStatusUpdate: (String, Boolean) -> Unit,
        onClickDelete: (String, Spanned?) -> Unit,
        onClickReplyComment: (String, Spanned?) -> Unit,
    ) {
        val navigationCallback = LocalNavigationCallback.current
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .clickable {
                    if (user != null) {
                        navigationCallback.onUserClick(user, 1f)
                    }
                }
                .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 8.dp)
        ) {
            val image = user?.avatar?.large
            if (loading || image != null) {
                AsyncImage(
                    model = image,
                    contentDescription = stringResource(R.string.anime_user_image),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .placeholder(
                            visible = loading,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name ?: "USERNAME",
                    modifier = Modifier.placeholder(
                        visible = loading,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
                )

                ThreadCommentTimestamp(
                    loading = loading,
                    aniListTimestamp = createdAt,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (viewer != null && viewer.id == user?.id) {
                    IconButton(onClick = {
                        if (commentId != null) {
                            onClickDelete(commentId, commentMarkdown)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(
                                R.string.anime_forum_thread_comment_delete_icon_content_description
                            ),
                        )
                    }
                }

                if (loading || likeCount > 0) {
                    Text(
                        text = likeCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .placeholder(
                                visible = loading,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                }
                IconButton(
                    enabled = viewer != null,
                    onClick = {
                        if (commentId != null) {
                            onStatusUpdate(commentId, !liked)
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (liked) {
                            Icons.Filled.ThumbUp
                        } else {
                            Icons.Outlined.ThumbUp
                        },
                        contentDescription = stringResource(
                            R.string.anime_forum_thread_comment_like_icon_content_description
                        ),
                    )
                }

                Box {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(
                                R.string.anime_forum_thread_more_actions_content_description,
                            ),
                        )
                    }

                    val uriHandler = LocalUriHandler.current
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.anime_forum_thread_open_in_browser)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.OpenInBrowser,
                                    contentDescription = stringResource(
                                        R.string.anime_forum_thread_open_in_browser_content_description
                                    )
                                )
                            },
                            onClick = {
                                showMenu = false
                                if (commentId != null) {
                                    uriHandler.openForceExternal(
                                        AniListUtils.forumThreadCommentUrl(
                                            viewModel.threadId,
                                            commentId,
                                        )
                                    )
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.anime_forum_thread_comment_reply)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Reply,
                                    contentDescription = stringResource(
                                        R.string.anime_forum_thread_comment_reply_content_description
                                    )
                                )
                            },
                            onClick = {
                                showMenu = false
                                if (commentId != null) {
                                    onClickReplyComment(commentId, commentMarkdown)
                                }
                            }
                        )
                    }
                }
            }
        }

        MarkdownText(
            text = commentMarkdown,
            modifier = Modifier
                .conditionally(loading) { fillMaxWidth() }
                .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                .placeholder(
                    visible = loading,
                    highlight = PlaceholderHighlight.shimmer(),
                )
        )
    }

    @Composable
    private fun ColumnScope.ThreadCommentChild(
        viewModel: ForumThreadViewModel,
        viewer: AuthedUserQuery.Data.Viewer?,
        level: Int,
        child: ForumThreadViewModel.CommentChild,
        onStatusUpdate: (String, Boolean) -> Unit,
        onClickDelete: (String, Spanned?) -> Unit,
        onClickReplyComment: (String, Spanned?) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(start = 16.dp + (8.dp * (level - 1)))
                // TODO: Better border drawing, currently very primitive
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RectangleShape,
                )
        ) {
            ThreadCommentContent(
                viewModel = viewModel,
                viewer = viewer,
                loading = false,
                commentId = child.id,
                commentMarkdown = child.commentMarkdown,
                createdAt = child.createdAt,
                liked = child.liked,
                likeCount = child.likeCount ?: 0,
                user = child.user,
                onStatusUpdate = onStatusUpdate,
                onClickDelete = onClickDelete,
                onClickReplyComment = onClickReplyComment,
            )
        }

        child.childComments.forEach {
            ThreadCommentChild(
                viewModel = viewModel,
                viewer = viewer,
                level = level + 1,
                child = it,
                onStatusUpdate = onStatusUpdate,
                onClickDelete = onClickDelete,
                onClickReplyComment = onClickReplyComment,
            )
        }
    }

    @Composable
    private fun PageIndicator(
        page: Int,
        pageString: String,
        maxPage: Int,
        onPageChange: (Int) -> Unit,
        onPageStringChange: (String) -> Unit,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    onPageChange(1)
                    onPageStringChange("1")
                },
                modifier = Modifier.alpha(if (page > 1) 1f else 0f)
            ) {
                Icon(
                    imageVector = Icons.Filled.FirstPage,
                    contentDescription = stringResource(
                        R.string.anime_forum_thread_comments_page_first_content_description
                    )
                )
            }
            IconButton(
                onClick = {
                    onPageChange(page - 1)
                    onPageStringChange("${page - 1}")
                },
                modifier = Modifier.alpha(if (page > 1) 1f else 0f)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(
                        R.string.anime_forum_thread_comments_page_back_content_description
                    )
                )
            }

            MinWidthTextField(
                value = pageString,
                onValueChange = {
                    onPageStringChange(it)
                    val asInt = it.trim().toIntOrNull()
                    if (asInt != null && asInt >= 1 && asInt <= maxPage) {
                        onPageChange(asInt)
                    }
                },
                suffix = { Text(text = "/$maxPage") },
                textStyle = LocalTextStyle.current
                    .copy(textAlign = TextAlign.Center),
                minWidth = 80.dp,
                modifier = Modifier
                    .widthIn(min = 80.dp)
                    .heightIn(min = 0.dp)
            )

            IconButton(
                onClick = {
                    onPageChange(page + 1)
                    onPageStringChange("${page + 1}")
                },
                modifier = Modifier.alpha(if (page < maxPage) 1f else 0f)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = stringResource(
                        R.string.anime_forum_thread_comments_page_forward_content_description
                    )
                )
            }
            IconButton(
                onClick = {
                    onPageChange(maxPage)
                    onPageStringChange("$maxPage")
                },
                modifier = Modifier.alpha(if (page < maxPage) 1f else 0f)
            ) {
                Icon(
                    imageVector = Icons.Filled.LastPage,
                    contentDescription = stringResource(
                        R.string.anime_forum_thread_comments_page_last_content_description
                    )
                )
            }
        }
    }
}
