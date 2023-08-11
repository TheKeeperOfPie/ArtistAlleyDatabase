package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

import android.os.SystemClock
import android.text.Spanned
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.anilist.fragment.MediaCompactWithTags
import com.anilist.type.MediaListStatus
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
import com.thekeeperofpie.artistalleydatabase.anilist.AniListPagingSource
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AniListOAuthStore
import com.thekeeperofpie.artistalleydatabase.anilist.oauth.AuthedAniListApi
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import com.thekeeperofpie.artistalleydatabase.anime.R
import com.thekeeperofpie.artistalleydatabase.anime.forum.ForumUtils
import com.thekeeperofpie.artistalleydatabase.anime.forum.thread.comment.ForumCommentEntry
import com.thekeeperofpie.artistalleydatabase.anime.forum.toForumThreadComment
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeMediaIgnoreList
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaListStatusController
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaUtils
import com.thekeeperofpie.artistalleydatabase.anime.media.applyMediaFiltering
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.utils.enforceUniqueIntIds
import dagger.hilt.android.lifecycle.HiltViewModel
import io.noties.markwon.Markwon
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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ForumThreadViewModel @Inject constructor(
    private val aniListApi: AuthedAniListApi,
    savedStateHandle: SavedStateHandle,
    val markwon: Markwon,
    mediaListStatusController: MediaListStatusController,
    threadStatusController: ForumThreadStatusController,
    commentStatusController: ForumThreadCommentStatusController,
    val ignoreList: AnimeMediaIgnoreList,
    settings: AnimeSettings,
    oAuthStore: AniListOAuthStore,
) : ViewModel() {

    // TODO: Block forum screens if not unlocked
    val hasAuth = oAuthStore.hasAuth

    val colorMap = mutableStateMapOf<String, Pair<Color, Color>>()
    val threadId = savedStateHandle.get<String>("threadId")!!
    val viewer = aniListApi.authedUser
    val refresh = MutableStateFlow(-1L)
    var entry by mutableStateOf(LoadingResult.loading<ForumThreadEntry>())
    var media by mutableStateOf<List<MediaEntry>>(emptyList())
    val comments = MutableStateFlow(PagingData.empty<ForumCommentEntry>())

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
                    val bodyMarkdown = thread.thread.body?.let(markwon::toMarkdown)
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
                            .map { MediaEntry(it) }
                    }
                }
                .flatMapLatest { media ->
                    combine(
                        mediaListStatusController.allChanges(media.map { it.media.id.toString() }
                            .toSet()),
                        ignoreList.updates,
                        settings.showAdult,
                        settings.showLessImportantTags,
                        settings.showSpoilerTags,
                    ) { updates, ignoredIds, showAdult, showLessImportantTags, showSpoilerTags ->
                        media.mapNotNull {
                            applyMediaFiltering(
                                updates,
                                ignoredIds = ignoredIds,
                                showAdult = showAdult,
                                showIgnored = true,
                                showLessImportantTags = showLessImportantTags,
                                showSpoilerTags = showSpoilerTags,
                                entry = it,
                                transform = { it },
                                media = it.media,
                                copy = { mediaListStatus, progress, progressVolumes, ignored, showLessImportantTags, showSpoilerTags ->
                                    copy(
                                        mediaListStatus = mediaListStatus,
                                        progress = progress,
                                        progressVolumes = progressVolumes,
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
                            ?.mapNotNull { ForumUtils.decodeChild(markwon, it) }
                            .orEmpty()
                        val commentMarkdown = it.comment?.let(markwon::toMarkdown)
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
                            pagingData.map {
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

    fun onClickReplyComment(commentId: String?, commentMarkdown: Spanned?) {
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
                    this@ForumThreadViewModel.replyData = null
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

    data class MediaEntry(
        override val media: MediaCompactWithTags,
        override val mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        override val progress: Int? = media.mediaListEntry?.progress,
        override val progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
        override val ignored: Boolean = false,
        override val showLessImportantTags: Boolean = false,
        override val showSpoilerTags: Boolean = false,
    ) : AnimeMediaCompactListRow.Entry {
        override val tags = MediaUtils.buildTags(media, showLessImportantTags, showSpoilerTags)
    }

    data class ReplyData(
        val id: String?,
        val text: Spanned?,
    )
}
