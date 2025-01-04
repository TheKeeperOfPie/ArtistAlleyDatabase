package com.thekeeperofpie.artistalleydatabase.alley.tags

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntry

actual data class SeriesWithArtistsEntry(
    actual val series: SeriesEntry,
    actual val artists: List<ArtistEntry>,
)
