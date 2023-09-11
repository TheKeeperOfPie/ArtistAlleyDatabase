package com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment

import android.os.SystemClock
import android.text.Spanned
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumUtils
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumUtils.decodeChild
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadCommentStatusController
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadCommentToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadEntry
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadStatusController
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.ForumThreadToggleHelper
import com.thekeeperofpie.artistalleydatabase.anime.ignore.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaCompactWithTagsEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.utils.toStableMarkdown
import com.thekeeperofpie.artistalleydatabase.compose.StableSpanned
import dagger.hilt.android.lifecycle.HiltViewModel
import io.noties.markwon.Markwon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ForumThreadCommentTreeViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    savedStateHandle: SavedStateHandle,
    val markwon: Markwon,
    mediaListStatusController: MediaListStatusController,
    threadStatusController: ForumThreadStatusController,
    commentStatusController: ForumThreadCommentStatusController,
    val ignoreController: IgnoreController,
    settings: AnimeSettings,
) : ViewModel() {

    val threadId = savedStateHandle.get<String>("threadId")!!
    val commentId = savedStateHandle.get<String>("commentId")!!
    val viewer = aniListApi.authedUser
    val refresh = MutableStateFlow(-1L)
    var entry by mutableStateOf(LoadingResult.loading<ForumThreadEntry>())
    var media by mutableStateOf<List<MediaCompactWithTagsEntry>>(emptyList())
    var comments by mutableStateOf(LoadingResult.loading<List<ForumCommentEntry>>())
        private set

    var replyData by mutableStateOf<ReplyData?>(null)
    var committing by mutableStateOf(false)
    var deleting by mutableStateOf(false)
    var error by mutableStateOf<Pair<Int, Throwable?>?>(null)

    val threadToggleHelper =
        ForumThreadToggleHelper(aniListApi, threadStatusController, viewModelScope)
    val commentToggleHelper =
        ForumThreadCommentToggleHelper(aniListApi, commentStatusController, viewModelScope)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(refresh, R.string.anime_forum_thread_error_loading) {
                flowFromSuspend {
                    val thread = aniListApi.forumThread(threadId)
                    val bodyMarkdown = thread.thread.body?.let(markwon::toStableMarkdown)
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
                        settings.showAdult,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { updates, ignoredIds, showAdult, showLessImportantTags, showSpoilerTags ->
                        media.mapNotNull {
                            applyMediaFiltering(
                                statuses = updates,
                                ignoreController = ignoreController,
                                showAdult = showAdult,
                                showIgnored = true,
                                showLessImportantTags = showLessImportantTags,
                                showSpoilerTags = showSpoilerTags,
                                entry = it,
                                transform = { it },
                                media = it.media,
                                copy = { mediaListStatus, progress, progressVolumes, scoreRaw, ignored, showLessImportantTags, showSpoilerTags ->
                                    copy(
                                        mediaListStatus = mediaListStatus,
                                        progress = progress,
                                        progressVolumes = progressVolumes,
                                        scoreRaw = scoreRaw,
                                        ignored = ignored,
                                        showLessImportantTags = showLessImportantTags,
                                        showSpoilerTags = showSpoilerTags,
                                    )
                                }
                            )
                        }
                    }
                }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { media = it }
        }

        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(refresh, R.string.anime_forum_thread_error_loading) {
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
                                ?.mapNotNull { decodeChild(markwon, it) }
                                .orEmpty()
                            val commentMarkdown = it.comment?.let(markwon::toStableMarkdown)
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

    fun refresh() {
        refresh.value = SystemClock.uptimeMillis()
    }

    fun onClickReplyComment(commentId: String?, commentMarkdown: StableSpanned?) {
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
                    refresh.emit(SystemClock.uptimeMillis())
                    this@ForumThreadCommentTreeViewModel.replyData = null
                    committing = false
                }
            } catch (t: Throwable) {
                withContext(CustomDispatchers.Main) {
                    error = R.string.anime_forum_thread_error_replying to t
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
                    refresh.emit(SystemClock.uptimeMillis())
                    deleting = false
                }
            } catch (t: Throwable) {
                withContext(CustomDispatchers.Main) {
                    error = R.string.anime_forum_thread_error_deleting to t
                    deleting = false
                }
            }
        }
    }

    data class ReplyData(
        val id: String?,
        val text: StableSpanned?,
    )
}
