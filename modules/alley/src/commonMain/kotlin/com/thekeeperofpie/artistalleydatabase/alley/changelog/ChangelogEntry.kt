package com.thekeeperofpie.artistalleydatabase.alley.changelog

import com.thekeeperofpie.artistalleydatabase.alley.data.ArtistEntryAnimeExpo2026Changelog
import kotlinx.datetime.LocalDate

sealed interface ChangelogEntry {
    val date: LocalDate

    data class Artist(val artist: ArtistEntryAnimeExpo2026Changelog): ChangelogEntry {
        override val date = LocalDate.parse(artist.date)
    }

    data class StampRally(val stampRally: StampRallyChangelogEntry) : ChangelogEntry {
        override val date get() = stampRally.date
    }
}
