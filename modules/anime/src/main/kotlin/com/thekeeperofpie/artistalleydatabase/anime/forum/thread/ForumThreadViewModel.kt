package com.thekeeperofpie.artistalleydatabase.anime.forum.thread

import android.text.Spanned
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.text.getSpans
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.anilist.ForumThreadCommentsQuery
import com.anilist.fragment.ForumThread
import com.hoc081098.flowext.flowFromSuspend
import com.thekeeperofpie.artistalleydatabase.android_utils.LoadingResult
import com.thekeeperofpie.artistalleydatabase.android_utils.flowForRefreshableContent
import com.thekeeperofpie.artistalleydatabase.android_utils.kotlin.CustomDispatchers
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
import kotlinx.coroutines.launch
import java.util.Arrays
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ForumThreadViewModel @Inject constructor(
    aniListApi: AuthedAniListApi,
    savedStateHandle: SavedStateHandle,
    val markwon: Markwon,
) : ViewModel() {

    val threadId = savedStateHandle.get<String>("threadId")!!
    val viewer = aniListApi.authedUser
    val refresh = MutableStateFlow(-1L)
    var entry by mutableStateOf<LoadingResult<ThreadEntry>>(LoadingResult.loading())
    val comments = MutableStateFlow(PagingData.empty<CommentEntry>())

    var committing by mutableStateOf(false)
    var deleting by mutableStateOf(false)

    init {
        viewModelScope.launch(CustomDispatchers.Main) {
            flowForRefreshableContent(refresh, R.string.anime_forum_thread_error_loading) {
                flowFromSuspend {
                    val thread = aniListApi.forumThread(threadId)
                    val bodyMarkdown = thread.thread.body
                        ?.let(markwon::parse)
                        ?.let(markwon::render)
                    ThreadEntry(thread.thread, bodyMarkdown)
                }
            }
                .flowOn(CustomDispatchers.IO)
                .collectLatest { entry = it }
        }
        viewModelScope.launch(CustomDispatchers.Main) {
            refresh.flatMapLatest {
                Pager(config = PagingConfig(10)) {
                    AniListPagingSource {
                        val result = aniListApi.forumThreadComments(threadId, page = it)
                        result.page?.pageInfo to result.page?.threadComments?.filterNotNull()
                            .orEmpty()
                    }
                }.flow
            }
                .map { it.map(::CommentEntry) }
                .enforceUniqueIntIds { it.comment.id }
                .cachedIn(viewModelScope)
                .flowOn(CustomDispatchers.IO)
                .collectLatest(comments::emit)
        }
    }

    fun sendReply(text: String) {
        TODO("Not yet implemented")
    }

    data class ThreadEntry(
        val thread: ForumThread,
        val bodyMarkdown: Spanned?,
    )

    data class CommentEntry(
        val comment: ForumThreadCommentsQuery.Data.Page.ThreadComment,
        val liked: Boolean = comment.isLiked ?: false,
    )
}
