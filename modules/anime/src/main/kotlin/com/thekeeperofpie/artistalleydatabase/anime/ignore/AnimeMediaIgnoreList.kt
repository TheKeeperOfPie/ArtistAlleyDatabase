package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.collection.ArraySet
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings

class AnimeMediaIgnoreList(private val settings: AnimeSettings) {

    fun get(mediaId: Int) = settings.ignoredAniListMediaIds.value.contains(mediaId)
    fun get(mediaId: String) = get(mediaId.toInt())

    fun set(mediaId: String, ignored: Boolean) {
        val set = settings.ignoredAniListMediaIds.value
        val mediaIdAsInt = mediaId.toInt()
        if (set.contains(mediaIdAsInt) == ignored) return

        settings.ignoredAniListMediaIds.value = ArraySet<Int>(1).apply {
            addAll(set)
            if (ignored) {
                add(mediaIdAsInt)
            } else {
                remove(mediaIdAsInt)
            }
        }
    }
}
