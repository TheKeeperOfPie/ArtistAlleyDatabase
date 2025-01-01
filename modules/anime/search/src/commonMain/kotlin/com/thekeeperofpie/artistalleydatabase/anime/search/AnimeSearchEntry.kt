package com.thekeeperofpie.artistalleydatabase.anime.search

import com.thekeeperofpie.artistalleydatabase.entry.EntryId

sealed interface AnimeSearchEntry {

    val entryId: EntryId

    data class Media<MediaEntry>(
        val mediaId: String,
        val entry: MediaEntry,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("media", mediaId)
    }

    data class Character<CharacterEntry>(
        val characterId: String,
        val entry: CharacterEntry,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("character", characterId)
    }

    data class Staff<StaffEntry>(
        val staffId: String,
        val entry: StaffEntry,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("staff", staffId)
    }

    data class Studio<StudioEntry>(
        val studioId: String,
        val entry: StudioEntry,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("studio", studioId)
    }

    data class User<UserEntry>(
        val userId: String,
        val entry: UserEntry,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("user", userId)
    }
}
