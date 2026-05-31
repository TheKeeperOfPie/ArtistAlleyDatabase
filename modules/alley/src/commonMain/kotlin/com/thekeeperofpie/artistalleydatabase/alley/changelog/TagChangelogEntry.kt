package com.thekeeperofpie.artistalleydatabase.alley.changelog

import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026Changelog
import kotlinx.datetime.LocalDate

sealed interface TagChangelogEntry {
    val date: LocalDate

    data class Artist(val artist: ArtistEntryAnimeExpo2026Changelog): TagChangelogEntry {
        override val date = LocalDate.parse(artist.date)
    }

    data class StampRally(val stampRally: StampRallyChangelogEntry): TagChangelogEntry {
        override val date = stampRally.date
    }
}
