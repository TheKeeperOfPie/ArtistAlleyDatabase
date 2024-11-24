package com.thekeeperofpie.artistalleydatabase.anime.ignore

import androidx.paging.PagingSource
import com.anilist.data.type.MediaType
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.AnimeIgnoreDao
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.AnimeMediaIgnoreEntry
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreController
import com.thekeeperofpie.artistalleydatabase.anime.ignore.data.IgnoreSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

fun fakeIgnoreController(scope: CoroutineScope) = IgnoreController(
    scope = scope,
    ignoreDao = object : AnimeIgnoreDao {
        override fun getEntries(type: MediaType): PagingSource<Int, AnimeMediaIgnoreEntry> {
            TODO("Not yet implemented")
        }

        override suspend fun getEntries(
            limit: Int,
            offset: Int,
            type: MediaType,
        ): List<AnimeMediaIgnoreEntry> {
            TODO("Not yet implemented")
        }

        override suspend fun insertEntries(vararg entries: AnimeMediaIgnoreEntry) {
            TODO("Not yet implemented")
        }

        override suspend fun getEntryCount(): Int {
            TODO("Not yet implemented")
        }

        override fun entryCountFlow(): Flow<Int> {
            TODO("Not yet implemented")
        }

        override suspend fun getEntryAtIndex(
            index: Int,
            type: MediaType,
        ): AnimeMediaIgnoreEntry? {
            TODO("Not yet implemented")
        }

        override suspend fun exists(id: String): Boolean {
            TODO("Not yet implemented")
        }

        override suspend fun delete(id: String) {
            TODO("Not yet implemented")
        }

        override suspend fun deleteAll() {
            TODO("Not yet implemented")
        }
    },
    settings = object : IgnoreSettings {
        override val mediaIgnoreEnabled = MutableStateFlow(false)
        override val mediaIgnoreHide = MutableStateFlow(false)
        override val showIgnored = MutableStateFlow(true)

    }
)
