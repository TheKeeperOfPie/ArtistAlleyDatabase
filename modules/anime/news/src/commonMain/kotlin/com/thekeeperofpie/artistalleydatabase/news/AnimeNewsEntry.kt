package com.thekeeperofpie.artistalleydatabase.news

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class AnimeNewsEntry<Category>(
    val id: String,
    val type: AnimeNewsType,
    val icon: String?,
    val image: String?,
    val title: String?,
    val description: String?,
    val link: String?,
    val copyright: String?,
    val date: Instant?,
    val categories: List<Category>,
)
