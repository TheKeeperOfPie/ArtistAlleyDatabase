package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.anilist.fragment.MediaPreviewWithDescription

data class AnimeHomeDataEntry(
    private val ignoredIds: Set<Int>,
    private val showIgnored: Boolean,
    private val lists: List<Triple<String, Int, List<MediaPreviewWithDescription?>?>>,
) {
    val data = lists.map { Triple(it.first, it.second, it.third.filterIgnored()) }

    private fun List<MediaPreviewWithDescription?>?.filterIgnored() =
        this?.filterNotNull()
            ?.mapNotNull {
                val ignored = ignoredIds.contains(it.id)
                if (showIgnored || !ignored) MediaEntry(it, ignored) else null
            }
            .orEmpty()

    class MediaEntry(
        val media: MediaPreviewWithDescription,
        ignored: Boolean,
    ) {
        var ignored by mutableStateOf(ignored)
    }
}
