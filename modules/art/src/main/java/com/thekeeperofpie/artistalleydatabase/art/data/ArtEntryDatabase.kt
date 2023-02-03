package com.thekeeperofpie.artistalleydatabase.art.data

import com.thekeeperofpie.artistalleydatabase.art.search.ArtEntryAdvancedSearchDao

interface ArtEntryDatabase {
    fun artEntryDao(): ArtEntryDao
    fun artEntryDetailsDao(): ArtEntryDetailsDao
    fun artEntryBrowseDao(): ArtEntryBrowseDao
    fun artEntrySyncDao(): ArtEntrySyncDao
    fun artEntryAdvancedSearchDao(): ArtEntryAdvancedSearchDao
}