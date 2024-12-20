package com.thekeeperofpie.artistalleydatabase.anime.forums.data

import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.runningFold
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class ForumThreadCommentStatusController {

    private val updates = MutableSharedFlow<Update>(replay = 0, extraBufferCapacity = 5)

    fun onUpdate(update: Update) = updates.tryEmit(update)

    fun allChanges() = updates.runningFold(emptyMap<String, Update>()) { acc, value ->
        acc + (value.commentId to value)
    }

    fun allChanges(filterIds: Set<String>) =
        updates.runningFold(emptyMap<String, Update>()) { acc, value ->
            if (filterIds.contains(value.commentId)) {
                acc + (value.commentId to value)
            } else {
                acc
            }
        }

    fun allChanges(filterCommentId: String) = updates
        .filter { it.commentId == filterCommentId }
        .startWith(null)

    data class Update(
        val commentId: String,
        val liked: Boolean?,
        val pending: Boolean = false,
        val error: Throwable? = null,
    )
}
