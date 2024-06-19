package com.thekeeperofpie.artistalleydatabase.alley

import com.thekeeperofpie.artistalleydatabase.alley.artist.ArtistEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.rallies.StampRallyEntryDao
import com.thekeeperofpie.artistalleydatabase.alley.tags.TagEntryDao

interface ArtistAlleyDatabase {
    fun artistEntryDao(): ArtistEntryDao
    fun stampRallyEntryDao(): StampRallyEntryDao
    fun tagEntryDao(): TagEntryDao
}
