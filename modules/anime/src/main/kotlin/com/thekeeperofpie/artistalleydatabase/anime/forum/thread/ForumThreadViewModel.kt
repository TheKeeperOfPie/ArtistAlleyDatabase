package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

import android.text.Spanned
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.anilist.ForumThread_CommentsQuery
import com.anilist.fragment.ForumThread
import com.anilist.fragment.UserNavigationData
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.transformIf
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIntIds
import dagger.hilt.android.lifecycle.HiltViewModel
import io.noties.markwon.Markwon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ForumThreadViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    savedStateHandle: SavedStateHandle,
    val markwon: Markwon,
    threadStatusController: ForumThreadStatusController,
    commentStatusController: ForumThreadCommentStatusController,
) : ViewModel() {

    val threadId = savedStateHandle.get<String>("threadId")!!
    val viewer = aniListApi.authedUser
    val refresh = MutableStateFlow(-1L)
    var entry by mutableStateOf<LoadingResult<ThreadEntry>>(LoadingResult.loading())
    val comments = MutableStateFlow(PagingData.empty<CommentEntry>())

    var committing by mutableStateOf(false)
    var deleting by mutableStateOf(false)

    val threadToggleHelper =
        ForumThreadToggleHelper(aniListApi, threadStatusController, viewModelScope)
    val commentToggleHelper =
        ForumThreadCommentToggleHelper(aniListApi, commentStatusController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(refresh, R.string.anime_forum_thread_error_loading) {
                flowFromSuspend {
                    val thread = aniListApi.forumThread(threadId)
                    val bodyMarkdown = thread.thread.body
                        ?.let(markwon::parse)
                        ?.let(markwon::render)
                    ThreadEntry(
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
                                val liked = update?.liked ?: it.liked
                                val subscribed = update?.subscribed ?: it.subscribed
                                it.transformIf(liked != it.liked || subscribed != it.subscribed) {
                                    copy(liked = liked, subscribed = subscribed)
                                }
                            }
                        }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }
        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.flatMapLatest {
                // TODO: Make an AniListPager which forces jumpThreshold to skip network requests
                Pager(config = PagingConfig(pageSize = 10, jumpThreshold = 10)) {
                    AniListPagingSource {
                        val result = aniListApi.forumThreadComments(threadId, page = it)
                        result.page?.pageInfo to result.page?.threadComments?.filterNotNull()
                            .orEmpty()
                    }
                }.flow
            }
                .map {
                    it.map {
                        val children = (it.childComments as? List<*>)?.filterNotNull()
                            ?.mapNotNull(::decodeChild)
                            .orEmpty()
                        val commentMarkdown = it.comment
                            ?.let(markwon::parse)
                            ?.let(markwon::render)
                        CommentEntry(
                            comment = it,
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
                            pagingData.map {
                                val liked = updates[it.comment.id.toString()]?.liked
                                    ?: it.comment.isLiked
                                    ?: false
                                it.copy(
                                    liked = liked,
                                    children = it.children.map { copyUpdatedChild(it, updates) },
                                )
                            }
                        }
                }
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(comments::emit)
        }
    }

    fun sendReply(text: String) {
        TODO("Not yet implemented")
    }

    // TODO: This way of copying is incredibly messy and expensive
    private fun copyUpdatedChild(
        child: CommentChild,
        updates: Map<String, ForumThreadCommentStatusController.Update>,
    ): CommentChild {
        val needsCopy = needsUpdate(updates, child)
        return child.transformIf(needsCopy) {
            val liked = updates[child.id]?.liked ?: child.liked
            copy(
                liked = liked,
                childComments = child.childComments.map { copyUpdatedChild(it, updates) },
            )
        }
    }

    private fun needsUpdate(
        updates: Map<String, ForumThreadCommentStatusController.Update>,
        child: CommentChild,
    ): Boolean {
        val update = updates[child.id]
        if (update != null && update.liked != child.liked) return true
        return child.childComments.any { needsUpdate(updates, it) }
    }

    private fun decodeChild(value: Any): CommentChild? {
        try {
            val map = (value as? Map<*, *>) ?: return null
            val id = map["id"] as? Int ?: return null
            val userMap = map["user"] as? Map<*, *> ?: return null
            val userId = userMap["id"] as? Int ?: return null
            val userName = userMap["name"] as? String ?: return null
            val userAvatarLarge = (userMap["avatar"] as? Map<*, *>)?.get("large") as? String
            val user = CommentChild.User(
                id = userId,
                avatar = userAvatarLarge?.let(CommentChild.User::Avatar),
                name = userName,
            )

            val createdAt = map["createdAt"] as? Int
            val likeCount = map["likeCount"] as? Int
            val comment = map["comment"] as? String
            val isLiked = map["isLiked"] as? Boolean ?: false
            val commentMarkdown = comment
                ?.let(markwon::parse)
                ?.let(markwon::render)

            val childComments = (map["childComments"] as? List<*>)
                ?.filterNotNull()
                ?.mapNotNull(::decodeChild)
                .orEmpty()

            return CommentChild(
                id = id.toString(),
                createdAt = createdAt,
                likeCount = likeCount,
                user = user,
                commentMarkdown = commentMarkdown,
                liked = isLiked,
                childComments = childComments,
            )
        } catch (ignored: Throwable) {
            return null
        }
    }

    data class ThreadEntry(
        val thread: ForumThread,
        val bodyMarkdown: Spanned?,
        val liked: Boolean,
        val subscribed: Boolean,
    )

    data class CommentEntry(
        val comment: ForumThread_CommentsQuery.Data.Page.ThreadComment,
        val commentMarkdown: Spanned?,
        val liked: Boolean = comment.isLiked ?: false,
        val children: List<CommentChild>,
        val user: UserNavigationData? = comment.user?.let {
            CommentChild.User(
                id = it.id,
                avatar = it.avatar?.large?.let(CommentChild.User::Avatar),
                name = it.name,
            )
        },
    )

    data class CommentChild(
        val id: String,
        val createdAt: Int? = null,
        val likeCount: Int? = null,
        val user: User,
        val commentMarkdown: Spanned? = null,
        val liked: Boolean = false,
        val childComments: List<CommentChild>,
    ) {

        data class User(
            override val id: Int,
            override val avatar: UserNavigationData.Avatar?,
            override val name: String,
        ) : UserNavigationData {

            data class Avatar(
                override val large: String?,
            ) : UserNavigationData.Avatar
        }
    }
}
