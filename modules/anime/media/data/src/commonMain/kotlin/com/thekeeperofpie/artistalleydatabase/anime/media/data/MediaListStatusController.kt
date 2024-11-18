package com.thekeeperofpie.artistalleydatabase.anime.media.data

import com.anilist.data.fragment.MediaDetailsListEntry
import com.hoc081098.flowext.startWith
import com.thekeeperofpie.artistalleydatabase.inject.SingletonScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.runningFold
import me.tatarka.inject.annotations.Inject

@SingletonScope
@Inject
class MediaListStatusController {

    private val updates = MutableSharedFlow<Update>(replay = 0, extraBufferCapacity = 5)

    suspend fun onUpdate(
        mediaId: String,
        entry: MediaDetailsListEntry?,
    ) = updates.emit(Update(mediaId = mediaId, entry = entry))

    fun allChanges() = updates.runningFold(emptyMap<String, Update>()) { acc, value ->
        acc + (value.mediaId to value)
    }

    fun allChanges(filterIds: Set<String>) =
        updates.runningFold(emptyMap<String, Update>()) { acc, value ->
            if (filterIds.contains(value.mediaId)) {
                acc + (value.mediaId to value)
            } else {
                acc
            }
        }

    fun allChanges(filterMediaId: String) = updates
        .filter { it.mediaId == filterMediaId }
        .startWith(null)

    data class Update(
        val mediaId: String,
        val entry: MediaDetailsListEntry? = null,
    )
}
