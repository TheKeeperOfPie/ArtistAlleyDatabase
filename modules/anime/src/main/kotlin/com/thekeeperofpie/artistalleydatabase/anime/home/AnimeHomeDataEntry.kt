package com.thekeeperofpie.artistalleydatabase.anime.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.anilist.fragment.MediaPreviewWithDescription

data class AnimeHomeDataEntry(
    private val ignoredIds: Set<Int>,
    private val showIgnored: Boolean,
    private val lists: List<AnimeHomeMediaViewModel.RowInput>,
) {
    val data = lists.map { RowData(it.id, it.titleRes, it.list.filterIgnored(), it.viewAllRoute) }

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

    data class RowData(
        val id: String,
        val titleRes: Int,
        val entries: List<MediaEntry>,
        val viewAllRoute: String?,
    )
}
