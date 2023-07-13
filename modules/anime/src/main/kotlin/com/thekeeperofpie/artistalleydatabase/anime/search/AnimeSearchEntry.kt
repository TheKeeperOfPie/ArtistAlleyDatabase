package com.thekeeperofpie.artistalleydatabase.anime.search

import com.anilist.CharacterAdvancedSearchQuery
import com.anilist.StaffSearchQuery
import com.anilist.UserSearchQuery
import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.user.UserListRow
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

sealed interface AnimeSearchEntry {

    val entryId: EntryId

    class Media<MediaType : MediaPreview>(
        media: MediaType,
        mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        ignored: Boolean = false,
    ) : AnimeMediaListRow.Entry<MediaType>(media, mediaListStatus, ignored), AnimeSearchEntry,
        MediaStatusAware {
        override val entryId = EntryId("media", media.id.toString())
    }

    class Character(
        character: CharacterAdvancedSearchQuery.Data.Page.Character
    ) : CharacterListRow.Entry(character), AnimeSearchEntry {
        override val entryId = EntryId("character", character.id.toString())
    }

    class Staff(
        staff: StaffSearchQuery.Data.Page.Staff
    ) : StaffListRow.Entry(staff), AnimeSearchEntry {
        override val entryId = EntryId("staff", staff.id.toString())
    }

    class User(
        user: UserSearchQuery.Data.Page.User
    ) : UserListRow.Entry(user), AnimeSearchEntry {
        override val entryId = EntryId("user", user.id.toString())
    }
}
