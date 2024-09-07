package com.thekeeperofpie.artistalleydatabase.anime.activity

import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.runningFold
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class ActivityReplyStatusController {

    private val updates = MutableSharedFlow<Update>(replay = 0, extraBufferCapacity = 5)

    fun onUpdate(update: Update) = updates.tryEmit(update)

    fun allChanges() = updates.runningFold(emptyMap<String, Update>()) { acc, value ->
        acc + (value.activityReplyId to value)
    }

    fun allChanges(filterIds: Set<String>) =
        updates.runningFold(emptyMap<String, Update>()) { acc, value ->
            if (filterIds.contains(value.activityReplyId)) {
                acc + (value.activityReplyId to value)
            } else {
                acc
            }
        }

    fun allChanges(filterActivityReplyId: String) = updates
        .filter { it.activityReplyId == filterActivityReplyId }
        .startWith(null)

    data class Update(
        val activityReplyId: String,
        val liked: Boolean?,
        val pending: Boolean = false,
        val error: Throwable? = null,
    )
}
