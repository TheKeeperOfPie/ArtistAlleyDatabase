package com.thekeeperofpie.artistalleydatabase.anime.search

import com.anilist.data.fragment.MediaPreviewWithDescription
import com.thekeeperofpie.artistalleydatabase.anime.characters.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.data.toNextAiringEpisode
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaPreviewWithDescriptionEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.MediaWithListStatusEntry
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaCompactListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaLargeCard
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.ui.MediaGridCard
import com.thekeeperofpie.artistalleydatabase.anime.staff.StaffListRow
import com.thekeeperofpie.artistalleydatabase.anime.studios.StudioListRowFragmentEntry
import com.thekeeperofpie.artistalleydatabase.anime.users.UserListRow
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

sealed interface AnimeSearchEntry {

    val entryId: EntryId

    data class Media(
        val entry: MediaPreviewWithDescriptionEntry,
    ) : AnimeSearchEntry, AnimeMediaListRow.Entry by entry,
        AnimeMediaLargeCard.Entry by entry, MediaGridCard.Entry by entry,
        AnimeMediaCompactListRow.Entry by entry {

        override val entryId = EntryId("media", media.id.toString())
        override val color get() = entry.color
        override val type get() = entry.type
        override val averageScore get() = entry.averageScore

        // TODO: Clean up below
        override val description get() = entry.description
        override val tags get() = entry.tags

        override val media get() = entry.media
        override val mediaListStatus get() = entry.mediaListStatus
        override val progress get() = entry.progress
        override val progressVolumes get() = entry.progressVolumes
        override val scoreRaw get() = entry.scoreRaw
        override val ignored get() = entry.ignored

        override val chapters get() = media.chapters
        override val episodes get() = media.episodes
        override val volumes get() = media.volumes
        override val nextAiringEpisode = media.nextAiringEpisode?.toNextAiringEpisode()

        constructor(media: MediaPreviewWithDescription) : this(
            MediaPreviewWithDescriptionEntry(media)
        )
    }

    data class Character(
        val entry: CharacterListRow.Entry<MediaWithListStatusEntry>,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("character", entry.character.id.toString())
    }

    data class Staff(
        val entry: StaffListRow.Entry<MediaWithListStatusEntry>,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("staff", entry.staff.id.toString())
    }

    data class Studio(
        val entry: StudioListRowFragmentEntry<MediaWithListStatusEntry>,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("studio", entry.studio.id.toString())
    }

    data class User(
        val entry: UserListRow.Entry<MediaWithListStatusEntry>,
    ) : AnimeSearchEntry {
        override val entryId = EntryId("user", entry.user.id.toString())
    }
}
