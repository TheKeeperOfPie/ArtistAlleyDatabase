package com.thekeeperofpie.artistalleydatabase.art

import com.thekeeperofpie.artistalleydatabase.art.details.ArtEntryDetailsDao
import com.thekeeperofpie.artistalleydatabase.art.search.ArtEntryAdvancedSearchDao

interface ArtEntryDatabase {
    fun artEntryDao(): ArtEntryDao
    fun artEntryEditDao(): ArtEntryEditDao
    fun artEntryDetailsDao(): ArtEntryDetailsDao
    fun artEntryBrowseDao(): ArtEntryBrowseDao
    fun artEntryAdvancedSearchDao(): ArtEntryAdvancedSearchDao
}