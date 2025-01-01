package com.thekeeperofpie.artistalleydatabase.anime.users.data

interface UserEntryProvider<User, UserEntry, MediaEntry> {
    fun userEntry(user: User, media: List<MediaEntry>): UserEntry
    fun id(userEntry: UserEntry): String
    fun media(userEntry: UserEntry) : List<MediaEntry>
    fun copyUserEntry(entry: UserEntry, media: List<MediaEntry>): UserEntry
}
