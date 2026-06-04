package com.thekeeperofpie.artistalleydatabase.alley.changelog

import kotlinx.datetime.LocalDate

sealed interface TagChangelogEntry {
    val date: LocalDate

    data class Artist(val artist: ArtistChangelogEntry): TagChangelogEntry {
        override val date get() = artist.date
    }

    data class StampRally(val stampRally: StampRallyChangelogEntry) : TagChangelogEntry {
        override val date get() = stampRally.date
    }
}
