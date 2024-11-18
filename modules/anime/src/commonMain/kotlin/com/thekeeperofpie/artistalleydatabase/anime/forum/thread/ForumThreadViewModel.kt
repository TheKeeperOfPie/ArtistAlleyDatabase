package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import artistalleydatabase.modules.anime.generated.resources.Res
import artistalleydatabase.modules.anime.generated.resources.anime_forum_thread_error_deleting
import artistalleydatabase.modules.anime.generated.resources.anime_forum_thread_error_loading
import artistalleydatabase.modules.anime.generated.resources.anime_forum_thread_error_replying
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anilist.paging.AniListPager
import com.thekeeperofpie.artistalleydatabase.anime.AnimeDestination
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumUtils
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumCommentEntry
import com.thekeeperofpie.artistalleydatabase.anime.forum.toForumThreadComment
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
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
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.enforceUniqueIntIds
import com.thekeeperofpie.artistalleydatabase.utils_compose.paging.mapOnIO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class ForumThreadViewModel(
    private val aniListApi: AuthedAniListApi,
    @Assisted savedStateHandle: SavedStateHandle,
    navigationTypeMap: NavigationTypeMap,
    private val markdown: Markdown,
    mediaListStatusController: MediaListStatusController,
    threadStatusController: ForumThreadStatusController,
    commentStatusController: ForumThreadCommentStatusController,
    private val ignoreController: IgnoreController,
    settings: AnimeSettings,
    oAuthStore: AniListOAuthStore,
) : ViewModel() {

    private val destination =
        savedStateHandle.toDestination<AnimeDestination.ForumThread>(navigationTypeMap)

    // TODO: Block forum screens if not unlocked
    val hasAuth = oAuthStore.hasAuth

    val threadId = destination.threadId
    val viewer = aniListApi.authedUser
    val refresh = RefreshFlow()
    var entry by mutableStateOf(LoadingResult.loading<ForumThreadEntry>())
    var media by mutableStateOf<List<MediaCompactWithTagsEntry>>(emptyList())
    val comments = MutableStateFlow(PagingData.empty<ForumCommentEntry>())

    var replyData by mutableStateOf<ReplyData?>(null)
    var committing by mutableStateOf(false)
    var deleting by mutableStateOf(false)
    var error by mutableStateOf<Pair<StringResource, Throwable?>?>(null)

    val threadToggleHelper =
        ForumThreadToggleHelper(aniListApi, threadStatusController, viewModelScope)
    val commentToggleHelper =
        ForumThreadCommentToggleHelper(aniListApi, commentStatusController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(
                refresh.updates,
                Res.string.anime_forum_thread_error_loading
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
                .collectLatest { entry = it }
        }
        viewModelScope.launch(CustomDispatchers.Main) {
            snapshotFlow { entry }
                .flowOn(CustomDispatchers.Main)
                .mapLatest {
                    val mediaIds = it.result?.thread?.mediaCategories?.mapNotNull { it?.id }
                    if (mediaIds.isNullOrEmpty()) {
                        emptyList()
                    } else {
                        aniListApi.mediaByIds(mediaIds)
                            .map { MediaCompactWithTagsEntry(it) }
                    }
                }
                .flatMapLatest { media ->
                    combine(
                        mediaListStatusController.allChanges(media.map { it.media.id.toString() }
                            .toSet()),
                        ignoreController.updates(),
                        settings.mediaFilteringData(forceShowIgnored = true)
                    ) { updates, _, filteringData ->
                        media.mapNotNull {
                            applyMediaFiltering(
                                updates,
                                ignoreController = ignoreController,
                                filteringData = filteringData,
                                entry = it,
                                filterableData = it.mediaFilterable,
                                copy = { copy(mediaFilterable = it) },
                            )
                        }
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { media = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.updates
                .flatMapLatest {
                    // TODO: Make an AniListPager which forces jumpThreshold to skip network requests
                    AniListPager {
                        val result = aniListApi.forumThreadComments(threadId, page = it)
                        result.page?.pageInfo to result.page?.threadComments?.filterNotNull()
                            .orEmpty()
                    }
                }
                .map {
                    it.mapOnIO {
                        val children = (it.childComments as? List<*>)?.filterNotNull()
                            ?.mapNotNull { ForumUtils.decodeChild(markdown, it) }
                            .orEmpty()
                        val commentMarkdown = it.comment?.let(markdown::convertMarkdownText)
                        ForumCommentEntry(
                            comment = it.toForumThreadComment(),
                            commentMarkdown = commentMarkdown,
                            children = children,
                        )
                    }
                }
                .enforceUniqueIntIds { it.comment.id }
                .cachedIn(viewModelScope)
                .flatMapLatest { pagingData ->
                    commentStatusController.allChanges()
                        .mapLatest { updates ->
                            pagingData.mapOnIO {
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
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(comments::emit)
        }
    }

    fun onClickReplyComment(commentId: String?, commentMarkdown: MarkdownText?) {
        replyData = ReplyData(commentId, commentMarkdown)
    }

    fun sendReply(text: String) {
        val replyData = replyData
        if (committing || replyData == null) return
        committing = true
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
                    this@ForumThreadViewModel.replyData = null
                    committing = false
                }
            } catch (t: Throwable) {
                withContext(CustomDispatchers.Main) {
                    error = Res.string.anime_forum_thread_error_replying to t
                    committing = false
                }
            }
        }
    }

    fun deleteComment(commentId: String) {
        if (deleting) return
        deleting = true
        viewModelScope.launch(CustomDispatchers.IO) {
            try {
                aniListApi.deleteForumThreadComment(commentId)
                withContext(CustomDispatchers.Main) {
                    refresh.refresh()
                    deleting = false
                }
            } catch (t: Throwable) {
                withContext(CustomDispatchers.Main) {
                    error = Res.string.anime_forum_thread_error_deleting to t
                    deleting = false
                }
            }
        }
    }

    data class ReplyData(
        val id: String?,
        val text: MarkdownText?,
    )
}
