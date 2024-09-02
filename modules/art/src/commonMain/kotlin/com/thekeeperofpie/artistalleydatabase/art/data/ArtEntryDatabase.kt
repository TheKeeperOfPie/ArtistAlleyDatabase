package com.thekeeperofpie.artistalleydatabase.art.data

interface ArtEntryDatabase {
    fun artEntryDao(): ArtEntryDao
    fun artEntryDetailsDao(): ArtEntryDetailsDao
    fun artEntryBrowseDao(): ArtEntryBrowseDao
    fun artEntrySyncDao(): ArtEntrySyncDao
}
