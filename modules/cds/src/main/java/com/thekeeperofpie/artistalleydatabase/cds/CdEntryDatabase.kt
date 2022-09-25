package com.thekeeperofpie.artistalleydatabase.cds

interface CdEntryDatabase {
    fun cdEntryDao(): CdEntryDao
    fun cdEntryDetailsDao(): CdEntryDetailsDao
}