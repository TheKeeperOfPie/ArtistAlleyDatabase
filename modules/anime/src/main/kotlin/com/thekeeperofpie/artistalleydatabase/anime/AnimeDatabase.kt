package com.thekeeperofpie.artistalleydatabase.anime

import com.thekeeperofpie.artistalleydatabase.anime.history.AnimeHistoryDao

interface AnimeDatabase {
    fun animeHistoryDao(): AnimeHistoryDao
}
