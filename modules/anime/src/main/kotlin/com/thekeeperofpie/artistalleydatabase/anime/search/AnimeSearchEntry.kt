package com.thekeeperofpie.artistalleydatabase.anime.search

import com.anilist.fragment.MediaPreviewWithDescription
import com.anilist.type.MediaListStatus
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaStatusAware
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.studio.StudioListRow
import com.thekeeperofpie.artistalleydatabase.anime.user.UserListRow
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

sealed interface AnimeSearchEntry {

    val entryId: EntryId

    data class Media(
        val entry: MediaPreviewWithDescriptionEntry,
    ) : AnimeSearchEntry, AnimeMediaListRow.Entry by entry, MediaStatusAware by entry,
        AnimeMediaLargeCard.Entry by entry, MediaGridCard.Entry by entry,
        AnimeMediaCompactListRow.Entry by entry {
        override val entryId = EntryId("media", media.id.toString())
        override val color
            get() = entry.color
        override val type
            get() = entry.type
        override val maxProgress
            get() = entry.maxProgress
        override val maxProgressVolumes
            get() = entry.maxProgressVolumes
        override val averageScore
            get() = entry.averageScore

        // So that enough meaningful text is shown, strip any double newlines
        override val description
            get() = entry.description
        override val tags
            get() = entry.tags

        override val media
            get() = entry.media
        override val mediaListStatus
            get() = entry.mediaListStatus
        override val progress
            get() = entry.progress
        override val progressVolumes
            get() = entry.progressVolumes
        override val scoreRaw
            get() = entry.scoreRaw
        override val ignored
            get() = entry.ignored
        override val showLessImportantTags
            get() = entry.showLessImportantTags
        override val showSpoilerTags
            get() = entry.showSpoilerTags

        constructor(
            media: MediaPreviewWithDescription,
            mediaListStatus: MediaListStatus? = media.mediaListEntry?.status,
            progress: Int? = media.mediaListEntry?.progress,
            progressVolumes: Int? = media.mediaListEntry?.progressVolumes,
            scoreRaw: Double? = media.mediaListEntry?.score,
            ignored: Boolean = false,
            showLessImportantTags: Boolean = false,
            showSpoilerTags: Boolean = false,
        ) : this(
            MediaPreviewWithDescriptionEntry(
                media = media,
                mediaListStatus = mediaListStatus,
                progress = progress,
                progressVolumes = progressVolumes,
                scoreRaw = scoreRaw,
                ignored = ignored,
                showLessImportantTags = showLessImportantTags,
                showSpoilerTags = showSpoilerTags,
            )
        )
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
