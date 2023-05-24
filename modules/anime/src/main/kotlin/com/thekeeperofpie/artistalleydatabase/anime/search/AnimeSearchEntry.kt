package com.thekeeperofpie.artistalleydatabase.anime.search

import com.anilist.CharacterAdvancedSearchQuery
import com.anilist.fragment.AniListListRowMedia
import com.thekeeperofpie.artistalleydatabase.anime.character.CharacterListRow
import com.thekeeperofpie.artistalleydatabase.anime.media.AnimeMediaListRow
import com.thekeeperofpie.artistalleydatabase.entry.EntryId

sealed interface AnimeSearchEntry {

    val entryId: EntryId

    class Media<MediaType : AniListListRowMedia>(media: MediaType, ignored: Boolean) :
        AnimeMediaListRow.MediaEntry<MediaType>(media, ignored), AnimeSearchEntry {
        override val entryId = EntryId("media", media.id.toString())
    }

    class Character(
        character: CharacterAdvancedSearchQuery.Data.Page.Character
    ) : CharacterListRow.Entry(character), AnimeSearchEntry {
        override val entryId = EntryId("character", character.id.toString())
    }
}
