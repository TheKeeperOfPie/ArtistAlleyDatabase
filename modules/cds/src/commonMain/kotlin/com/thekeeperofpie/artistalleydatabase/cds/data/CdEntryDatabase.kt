package com.thekeeperofpie.artistalleydatabase.cds.data

interface CdEntryDatabase {
    fun cdEntryDao(): CdEntryDao
    fun cdEntryBrowseDao(): CdEntryBrowseDao
    fun cdEntryDetailsDao(): CdEntryDetailsDao
}