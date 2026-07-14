package com.thekeeperofpie.artistalleydatabase.alley.changelog

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import kotlinx.datetime.LocalDate

sealed interface ChangelogEntry {
    val date: LocalDate

    data class Artist(val artist: ArtistEntryDao.ArtistChangelogEntry): ChangelogEntry {
        override val date = LocalDate.parse(artist.date)
    }

    data class StampRally(val stampRally: StampRallyChangelogEntry) : ChangelogEntry {
        override val date get() = stampRally.date
    }
}
