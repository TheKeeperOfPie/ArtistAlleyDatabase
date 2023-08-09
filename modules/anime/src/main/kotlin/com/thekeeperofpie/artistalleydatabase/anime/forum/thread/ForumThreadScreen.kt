package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import com.anilist.AuthedUserQuery
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
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListScreen
import com.thekeeperofpie.artistalleydatabase.anime.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.anime.writing.WritingReplyPanelScaffold
import com.thekeeperofpie.artistalleydatabase.compose.AppBar
import com.thekeeperofpie.artistalleydatabase.compose.ImageHtmlText
import com.thekeeperofpie.artistalleydatabase.compose.UpIconOption
import com.thekeeperofpie.artistalleydatabase.compose.conditionally
import com.thekeeperofpie.artistalleydatabase.compose.openForceExternal
import java.time.Instant
import java.time.ZoneOffset

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
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val refresh by viewModel.refresh.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val entry = viewModel.entry
        val errorText = entry.error?.let { stringResource(it.first) }
        LaunchedEffect(errorText) {
            if (errorText != null) {
                snackbarHostState.showSnackbar(
                    message = errorText,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )
                viewModel.entry = viewModel.entry.copy(error = null)
            }
        }
        WritingReplyPanelScaffold(
            snackbarHostState = snackbarHostState,
            refreshEvent = refresh,
            committing = viewModel.committing,
            onClickSend = viewModel::sendReply,
            topBar = {
                AppBar(
                    text = title ?: stringResource(R.string.anime_forum_thread_default_title),
                    upIconOption = upIconOption,
                    scrollBehavior = scrollBehavior,
                )
            },
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            val comments = viewModel.comments.collectAsLazyPagingItems()
            val refreshState = comments.loadState.refresh
            val refreshing = refreshState is LoadState.Loading
            val pullRefreshState = rememberPullRefreshState(
                refreshing = refreshing,
                onRefresh = comments::refresh,
            )
            Box(
                modifier = Modifier
                    .padding(it)
                    .pullRefresh(pullRefreshState)
            ) {
                val viewer by viewModel.viewer.collectAsState()
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    val threadEntry = entry.result
                    item("threadHeader") {
                        ThreadHeader(viewModel, viewer, threadEntry)
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
                        items(
                            count = comments.itemCount,
                            key = comments.itemKey { it.comment.id },
                            contentType = comments.itemContentType { "comment" },
                        ) {
                            val commentEntry = comments[it]
                            ThreadComment(
                                viewer,
                                commentEntry,
                                onStatusUpdate = { _, _ -> },//viewModel.commentToggleHelper::toggleLike,
                                onClickDelete = {
                                    if (commentEntry != null) {
                                        // deletePromptData = Either.Right(replyEntry)
                                    }
                                },
                            )
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = refreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    @Composable
    private fun ThreadHeader(
        viewModel: ForumThreadViewModel,
        viewer: AuthedUserQuery.Data.Viewer?,
        entry: ForumThreadViewModel.ThreadEntry?,
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
                        user = thread?.replyUser,
                        aniListTimestamp = thread?.createdAt,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (thread != null) {
                        ThreadCategoryRow(thread)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    ThreadViewReplyCountIcons(
                        viewCount = thread?.viewCount,
                        replyCount = thread?.replyCount,
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

            if (thread == null || !thread.body.isNullOrBlank()) {
                val mayHaveSpoilers =
                    thread?.title?.contains("Spoilers", ignoreCase = true) == true
                var bodyShown by rememberSaveable { mutableStateOf(!mayHaveSpoilers) }
                if (bodyShown) {
                    MarkdownText(
                        markwon = viewModel.markwon,
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
                    Button(onClick = { bodyShown = true }) {
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

            HorizontalDivider()
        }
    }

    @Composable
    private fun ThreadComment(
        viewer: AuthedUserQuery.Data.Viewer?,
        entry: ForumThreadViewModel.CommentEntry?,
        onStatusUpdate: (String, Boolean) -> Unit,
        onClickDelete: (String) -> Unit,
    ) {
        // TODO: Child comments

        val comment = entry?.comment
        val user = comment?.user
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
                if (entry == null || image != null) {
                    AsyncImage(
                        model = image,
                        contentDescription = stringResource(R.string.anime_user_image),
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .placeholder(
                                visible = entry == null,
                                highlight = PlaceholderHighlight.shimmer(),
                            )
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.name ?: "USERNAME",
                        modifier = Modifier.placeholder(
                            visible = entry == null,
                            highlight = PlaceholderHighlight.shimmer(),
                        )
                    )

                    ThreadCommentTimestamp(
                        loading = entry == null,
                        aniListTimestamp = comment?.createdAt,
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (viewer != null && viewer.id == user?.id) {
                        IconButton(onClick = { onClickDelete(comment.id.toString()) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(
                                    R.string.anime_activity_reply_delete_content_description
                                ),
                            )
                        }
                    }

                    val likeCount = comment?.likeCount ?: 0
                    if (entry == null || likeCount > 0) {
                        Text(
                            text = likeCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .placeholder(
                                    visible = entry == null,
                                    highlight = PlaceholderHighlight.shimmer(),
                                )
                        )
                    }
                    IconButton(
                        enabled = viewer != null,
                        onClick = {
                            if (comment != null) {
                                onStatusUpdate(
                                    comment.id.toString(),
                                    !entry.liked,
                                )
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (entry?.liked == true) {
                                Icons.Filled.ThumbUp
                            } else {
                                Icons.Outlined.ThumbUp
                            },
                            contentDescription = stringResource(
                                R.string.anime_forum_thread_comment_like_icon_content_description
                            ),
                        )
                    }
                }
            }

            ImageHtmlText(
                text = comment?.comment.orEmpty(),
                color = MaterialTheme.typography.bodySmall.color
                    .takeOrElse { LocalContentColor.current },
                modifier = Modifier
                    .conditionally(entry == null) { fillMaxWidth() }
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                    .placeholder(
                        visible = entry == null,
                        highlight = PlaceholderHighlight.shimmer(),
                    )
            )

            Divider()
        }
    }
}
