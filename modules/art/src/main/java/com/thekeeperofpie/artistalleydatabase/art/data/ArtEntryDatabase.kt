package com.thekeeperofpie.artistalleydatabase.art.data

import com.thekeeperofpie.artistalleydatabase.art.search.ArtEntryAdvancedSearchDao

interface ArtEntryDatabase {
    fun artEntryDao(): ArtEntryDao
    fun artEntryEditDao(): ArtEntryEditDao
    fun artEntryDetailsDao(): ArtEntryDetailsDao
    fun artEntryBrowseDao(): ArtEntryBrowseDao
    fun artEntryAdvancedSearchDao(): ArtEntryAdvancedSearchDao
}