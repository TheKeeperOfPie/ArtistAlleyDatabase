package com.thekeeperofpie.artistalleydatabase.anime.users

data class UserStudiosEntry<StudioEntry>(
    val hasMore: Boolean = false,
    val studios: List<StudioEntry> = emptyList(),
)
