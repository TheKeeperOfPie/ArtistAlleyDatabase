package com.thekeeperofpie.artistalleydatabase.anime.forums.thread

import com.hoc081098.flowext.startWith
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.runningFold

@SingleIn(AppScope::class)
@Inject
class ForumThreadStatusController {

    private val updates = MutableSharedFlow<Update>(replay = 0, extraBufferCapacity = 5)

    fun onUpdate(update: Update) = updates.tryEmit(update)

    fun allChanges() = updates.runningFold(emptyMap<String, Update>()) { acc, value ->
        acc + (value.threadId to value)
    }

    fun allChanges(filterIds: Set<String>) =
        updates.runningFold(emptyMap<String, Update>()) { acc, value ->
            if (filterIds.contains(value.threadId)) {
                acc + (value.threadId to value)
            } else {
                acc
            }
        }

    fun allChanges(filterThreadId: String) = updates
        .filter { it.threadId == filterThreadId }
        .startWith(null)

    data class Update(
        val threadId: String,
        val liked: Boolean?,
        val subscribed: Boolean?,
        val pending: Boolean = false,
        val error: Throwable? = null,
    )
}
