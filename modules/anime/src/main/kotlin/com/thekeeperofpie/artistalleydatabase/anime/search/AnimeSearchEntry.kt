package com.thekeeperofpie.artistalleydatabase.anime.search

import com.anilist.fragment.MediaPreview
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.user.UserListRow
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

sealed interface AnimeSearchEntry {

    val entryId: EntryId

    class Media<MediaType : MediaPreview>(
        media: MediaType,
        mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
        progress: Int? = null,
        progressVolumes: Int? = null,
        ignored: Boolean = false,
        showLessImportantTags: Boolean = false,
        showSpoilerTags: Boolean = false,
    ) : AnimeMediaListRow.Entry<MediaType>(
        media = media,
        mediaListStatus = mediaListStatus,
        progress = progress,
        progressVolumes = progressVolumes,
        ignored = ignored,
        showLessImportantTags = showLessImportantTags,
        showSpoilerTags = showSpoilerTags,
    ), AnimeSearchEntry, MediaStatusAware {
        override val entryId = EntryId("media", media.id.toString())
    }

    data class Character(
        val entry: CharacterListRow.Entry,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("character", entry.character.id.toString())
    }

    data class Staff(
        val entry: StaffListRow.Entry,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("staff", entry.staff.id.toString())
    }

    data class Studio(
        val entry: StudioListRow.Entry,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("studio", entry.studio.id.toString())
    }

    data class User(
        val entry: UserListRow.Entry,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("user", entry.user.id.toString())
    }
}
