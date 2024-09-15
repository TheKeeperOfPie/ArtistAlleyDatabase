package com.thekeeperofpie.artistalleydatabase.anime.data

import androidx.compose.runtime.Immutable

@Immutable
data class Title(
    val userPreferred: String?,
    val romaji: String?,
    val english: String?,
    val native: String?,
)
