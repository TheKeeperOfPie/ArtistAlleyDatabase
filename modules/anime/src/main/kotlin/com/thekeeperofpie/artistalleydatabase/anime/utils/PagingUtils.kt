package com.thekeeperofpie.artistalleydatabase.anime.utils

import androidx.paging.PagingData
import androidx.paging.filter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

inline fun <T : Any> Flow<PagingData<T>>.enforceUniqueIds(
    crossinline id: suspend (value: T) -> String?,
) = map {
    // AniList can return duplicates across pages, manually enforce uniqueness
    val seenIds = mutableSetOf<String>()
    it.filter {
        @Suppress("NAME_SHADOWING") val id = id(it)
        if (id == null) false else seenIds.add(id)
    }
}

inline fun <T : Any> Flow<PagingData<T>>.enforceUniqueIntIds(
    crossinline id: suspend (value: T) -> Int?,
) = map {
    // AniList can return duplicates across pages, manually enforce uniqueness
    val seenIds = mutableSetOf<Int>()
    it.filter {
        @Suppress("NAME_SHADOWING") val id = id(it)
        if (id == null) false else seenIds.add(id)
    }
}
