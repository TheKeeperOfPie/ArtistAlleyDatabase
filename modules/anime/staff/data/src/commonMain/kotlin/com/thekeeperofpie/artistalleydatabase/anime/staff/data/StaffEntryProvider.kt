package com.thekeeperofpie.artistalleydatabase.anime.staff.data

interface StaffEntryProvider<Staff, StaffEntry, MediaEntry> {
    fun staffEntry(staff: Staff, media: List<MediaEntry>): StaffEntry
    fun id(staffEntry: StaffEntry): String
    fun media(staffEntry: StaffEntry) : List<MediaEntry>
    fun copyStaffEntry(entry: StaffEntry, media: List<MediaEntry>): StaffEntry
}
