package com.thekeeperofpie.artistalleydatabase.alley.changelog

import kotlinx.datetime.LocalDate

sealed interface FavoriteChangelogEntry {
    val date: LocalDate

    data class Artist(
        val artist: ArtistChangelogEntry,
        val favoriteSeries: Set<String>,
        val remainingSeries: Set<String>,
    ) : FavoriteChangelogEntry {
        override val date get() = artist.date
    }

    data class StampRally(
        val stampRally: StampRallyChangelogEntry,
        val favoriteSeries: Set<String>,
        val remainingSeries: Set<String>,
    ) : FavoriteChangelogEntry {
        override val date get() = stampRally.date
    }
}
