package com.thekeeperofpie.artistalleydatabase.alley.artist.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

interface ArtistSortFilterSettings {
    var artistsSortOption: ArtistSearchSortOption
    var artistsSortAscending: Boolean
    var artistsTagsIn: Set<ArtistTag>
    var artistsTagsNotIn: Set<ArtistTag>
    var showOutdatedCatalogs: Boolean

    companion object {
        @Composable
        fun rememberForPreview() = object : ArtistSortFilterSettings {
            override var artistsSortOption by mutableStateOf(ArtistSearchSortOption.RANDOM)
            override var artistsSortAscending by mutableStateOf(true)
            override var artistsTagsIn by mutableStateOf(setOf(ArtistTag.HAS_CATALOG))
            override var artistsTagsNotIn by mutableStateOf(setOf(ArtistTag.VERIFIED))
            override var showOutdatedCatalogs by mutableStateOf(true)
        }
    }
}
