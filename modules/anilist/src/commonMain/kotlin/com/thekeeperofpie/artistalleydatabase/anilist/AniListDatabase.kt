package com.thekeeperofpie.artistalleydatabase.anilist

import com.thekeeperofpie.artistalleydatabase.anilist.character.CharacterEntryDao
import com.thekeeperofpie.artistalleydatabase.anilist.media.MediaEntryDao

interface AniListDatabase {
    fun mediaEntryDao(): MediaEntryDao
    fun characterEntryDao(): CharacterEntryDao
}