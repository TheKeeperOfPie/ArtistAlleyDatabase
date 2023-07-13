package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.collection.ArraySet
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings
import kotlinx.coroutines.flow.MutableSharedFlow

class AnimeMediaIgnoreList(private val settings: AnimeSettings) {

    val updates = MutableSharedFlow<Set<Int>>(replay = 5, extraBufferCapacity = 5)
        .apply { tryEmit(settings.ignoredAniListMediaIds.value) }

    private fun get(mediaId: Int) = settings.ignoredAniListMediaIds.value.contains(mediaId)
    private fun get(mediaId: String) = get(mediaId.toInt())

    private fun set(mediaId: String, ignored: Boolean) {
        val set = settings.ignoredAniListMediaIds.value
        val mediaIdAsInt = mediaId.toInt()
        if (set.contains(mediaIdAsInt) == ignored) return

        val newSet = ArraySet<Int>(1).apply {
            addAll(set)
            if (ignored) {
                add(mediaIdAsInt)
            } else {
                remove(mediaIdAsInt)
            }
        }
        settings.ignoredAniListMediaIds.value = newSet
        updates.tryEmit(newSet)
    }

    fun toggle(mediaId: String) = set(mediaId, !get(mediaId))
}
