package com.thekeeperofpie.artistalleydatabase.anime.forums.thread

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.thekeeperofpie.artistalleydatabase.anime.forums.thread.comment.ForumCommentReplyData
import com.thekeeperofpie.artistalleydatabase.utils_compose.LoadingResult
import org.jetbrains.compose.resources.StringResource

@Stable
class ForumThreadState<MediaEntry> {
    var entry by mutableStateOf(LoadingResult.loading<ForumThreadEntry>())
    var media by mutableStateOf<List<MediaEntry>>(emptyList())
    var replyData by mutableStateOf<ForumCommentReplyData?>(null)
    var committing by mutableStateOf(false)
    var deleting by mutableStateOf(false)
    var error by mutableStateOf<Pair<StringResource, Throwable?>?>(null)
}
