package com.thekeeperofpie.artistalleydatabase.anime

import com.thekeeperofpie.artistalleydatabase.anime.history.AnimeHistoryDao
import com.thekeeperofpie.artistalleydatabase.anime.ignore.AnimeIgnoreDao

interface AnimeDatabase {
    fun animeHistoryDao(): AnimeHistoryDao
    fun animeIgnoreDao(): AnimeIgnoreDao
}
