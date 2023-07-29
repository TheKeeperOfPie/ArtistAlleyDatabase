package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.collection.ArraySet
import com.thekeeperofpie.artistalleydatabase.android_utils.FeatureOverrideProvider
import com.thekeeperofpie.artistalleydatabase.anime.AnimeSettings

class AnimeMediaIgnoreList(
    private val settings: AnimeSettings,
    private val featureOverrideProvider: FeatureOverrideProvider,
) {

    val updates = settings.ignoredAniListMediaIds

    private fun get(mediaId: Int) = if (featureOverrideProvider.isReleaseBuild) {
        false
    } else {
        settings.ignoredAniListMediaIds.value.contains(mediaId)
    }

    private fun get(mediaId: String) = get(mediaId.toInt())

    private fun set(mediaId: String, ignored: Boolean) {
        if (featureOverrideProvider.isReleaseBuild) return
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
    }

    fun toggle(mediaId: String) {
        if (!featureOverrideProvider.isReleaseBuild) {
            set(mediaId, !get(mediaId))
        }
    }
}
