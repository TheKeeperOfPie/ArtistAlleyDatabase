package com.thekeeperofpie.artistalleydatabase.cds.data

interface CdEntryDatabase {
    fun cdEntryDao(): CdEntryDao
    fun cdEntryDetailsDao(): CdEntryDetailsDao
}