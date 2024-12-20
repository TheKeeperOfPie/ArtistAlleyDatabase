package com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import artistalleydatabase.modules.anime.forums.generated.resources.Res
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_thread_error_deleting
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_thread_error_loading
import artistalleydatabase.modules.anime.forums.generated.resources.anime_forum_thread_error_replying
import com.anilist.data.fragment.MediaCompactWithTags
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.forums.ForumDestinations
import com.thekeeperofpie.artistalleydatabase.anime.forums.ForumUtils
import com.thekeeperofpie.artistalleydatabase.anime.forums.ForumUtils.decodeChild
import com.thekeeperofpie.artistalleydatabase.anime.forums.data.ForumThreadCommentStatusController
import com.thekeeperofpie.artistalleydatabase.anime.forums.data.ForumThreadCommentToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.ForumThreadEntry
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.ForumThreadState
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.ForumThreadStatusController
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.ForumThreadToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaDataSettings
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaEntryProvider
import com.thekeeperofpie.artistalleydatabase.anime.media.data.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.data.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.data.mediaFilteringData
import com.thekeeperofpie.artistalleydatabase.markdown.Markdown
import com.thekeeperofpie.artistalleydatabase.markdown.MarkdownText
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.utils.kotlin.RefreshFlow
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import com.thekeeperofpie.artistalleydatabase.utils_compose.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.NavigationTypeMap
import com.thekeeperofpie.artistalleydatabase.utils_compose.navigation.toDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ForumThreadCommentTreeViewModel<MediaEntry>(
    private val aniListApi: AuthedAniListApi,
    navigationTypeMap: NavigationTypeMap,
    private val markdown: Markdown,
    mediaListStatusController: MediaListStatusController,
    threadStatusController: ForumThreadStatusController,
    commentStatusController: ForumThreadCommentStatusController,
    private val ignoreController: IgnoreController,
    settings: MediaDataSettings,
    @Assisted savedStateHandle: SavedStateHandle,
    @Assisted private val mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<ForumDestinations.ForumThreadComment>(navigationTypeMap)
    val threadId = destination.threadId
    val commentId = destination.commentId
    val viewer = aniListApi.authedUser
    val refresh = RefreshFlow()
    val state = ForumThreadState<MediaEntry>()
    var comments by mutableStateOf(LoadingResult.loading<List<ForumCommentEntry>>())

    val threadToggleHelper =
        ForumThreadToggleHelper(aniListApi, threadStatusController, viewModelScope)
    val commentToggleHelper =
        ForumThreadCommentToggleHelper(aniListApi, commentStatusController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(
                refresh.updates,
                Res.string.anime_forum_thread_error_loading,
            ) {
                flowFromSuspend {
                    val thread = aniListApi.forumThread(threadId)
                    val bodyMarkdown = thread.thread.body?.let(markdown::convertMarkdownText)
                    ForumThreadEntry(
                        thread = thread.thread,
                        bodyMarkdown = bodyMarkdown,
                        liked = thread.thread.isLiked ?: false,
                        subscribed = thread.thread.isSubscribed ?: false,
                    )
                }
            }
                .flatMapLatest { result ->
                    threadStatusController.allChanges(threadId)
                        .mapLatest { update ->
                            result.transformResult {
                                val threadLiked = update?.liked ?: it.liked
                                val threadSubscribed = update?.subscribed ?: it.subscribed

                                it.copy(
                                    liked = threadLiked,
                                    subscribed = threadSubscribed,
                                )
                            }
                        }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { state.entry = it }
        }
        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { state.entry }
                .flowOn(CustomDispatchers.Main)
                .mapLatest {
                    val mediaIds = it.result?.thread?.mediaCategories?.mapNotNull { it?.id }
                    if (mediaIds.isNullOrEmpty()) {
                        emptyList()
                    } else {
                        aniListApi.mediaByIds(mediaIds)
                            .map(mediaEntryProvider::mediaEntry)
                    }
                }
                .flatMapLatest { media ->
                    combine(
                        mediaListStatusController
                            .allChanges(media.map(mediaEntryProvider::id).toSet()),
                        ignoreController.updates(),
                        settings.mediaFilteringData(forceShowIgnored = true),
                    ) { updates, _, filteringData ->
                        media.mapNotNull {
                            applyMediaFiltering(
                                statuses = updates,
                                ignoreController = ignoreController,
                                filteringData = filteringData,
                                entry = it,
                                filterableData = mediaEntryProvider.mediaFilterable(it),
                                copy = { mediaEntryProvider.copyMediaEntry(this, it) },
                            )
                        }
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { state.media = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(
                refresh.updates,
                Res.string.anime_forum_thread_error_loading,
            ) {
                flowFromSuspend {
                    aniListApi.forumThreadSingleCommentTree(threadId, commentId)
                        .threadComment
                        ?.filterNotNull()
                        ?.distinctBy { it.id }
                        .orEmpty()
                }
            }
                .mapLatest {
                    it.transformResult {
                        it.map {
                            val children = (it.childComments as? List<*>)?.filterNotNull()
                                ?.mapNotNull { decodeChild(markdown, it) }
                                .orEmpty()
                            val commentMarkdown = it.comment?.let(markdown::convertMarkdownText)
                            ForumCommentEntry(
                                comment = it,
                                commentMarkdown = commentMarkdown,
                                children = children,
                            )
                        }
                    }
                }
                .flatMapLatest { result ->
                    commentStatusController.allChanges()
                        .mapLatest { updates ->
                            result.transformResult {
                                it.map {
                                    val liked = updates[it.comment.id.toString()]?.liked
                                        ?: it.comment.isLiked
                                        ?: false
                                    it.copy(
                                        liked = liked,
                                        children = it.children.map {
                                            ForumUtils.copyUpdatedChild(it, updates)
                                        },
                                    )
                                }
                            }
                        }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { comments = it }
        }
    }

    fun refresh() = refresh.refresh()

    fun onClickReplyComment(commentId: String?, commentMarkdown: MarkdownText?) {
        state.replyData = ForumCommentReplyData(commentId, commentMarkdown)
    }

    fun sendReply(text: String) {
        val replyData = state.replyData
        if (state.committing || replyData == null) return
        state.committing = true
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                aniListApi.saveForumThreadComment(
                    threadId = threadId,
                    // TODO: Support editing comments
                    commentId = null,
                    parentCommentId = replyData.id,
                    text = text,
                )
                withContext(CustomDispatchers.Main) {
                    refresh.refresh()
                    state.replyData = null
                    state.committing = false
                }
            } catch (t: Throwable) {
                withContext(CustomDispatchers.Main) {
                    state.error = Res.string.anime_forum_thread_error_replying to t
                    state.committing = false
                }
            }
        }
    }

    fun deleteComment(commentId: String) {
        if (state.deleting) return
        state.deleting = true
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                aniListApi.deleteForumThreadComment(commentId)
                withContext(CustomDispatchers.Main) {
                    refresh.refresh()
                    state.deleting = false
                }
            } catch (t: Throwable) {
                withContext(CustomDispatchers.Main) {
                    state.error = Res.string.anime_forum_thread_error_deleting to t
                    state.deleting = false
                }
            }
        }
    }

    @Inject
    class Factory(
        private val aniListApi: AuthedAniListApi,
        private val navigationTypeMap: NavigationTypeMap,
        private val markdown: Markdown,
        private val mediaListStatusController: MediaListStatusController,
        private val threadStatusController: ForumThreadStatusController,
        private val commentStatusController: ForumThreadCommentStatusController,
        private val ignoreController: IgnoreController,
        private val settings: MediaDataSettings,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) {
        fun <MediaEntry> create(
            mediaEntryProvider: MediaEntryProvider<MediaCompactWithTags, MediaEntry>,
        ) = ForumThreadCommentTreeViewModel(
            aniListApi = aniListApi,
            navigationTypeMap = navigationTypeMap,
            markdown = markdown,
            mediaListStatusController = mediaListStatusController,
            threadStatusController = threadStatusController,
            commentStatusController = commentStatusController,
            ignoreController = ignoreController,
            settings = settings,
            savedStateHandle = savedStateHandle,
            mediaEntryProvider = mediaEntryProvider,
        )
    }
}
