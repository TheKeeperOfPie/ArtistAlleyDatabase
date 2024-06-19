package com.thekeeperofpie.artistalleydatabase.alley

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao

interface ArtistAlleyDatabase {
    fun artistEntryDao(): ArtistEntryDao
    fun stampRallyEntryDao(): StampRallyEntryDao
}
