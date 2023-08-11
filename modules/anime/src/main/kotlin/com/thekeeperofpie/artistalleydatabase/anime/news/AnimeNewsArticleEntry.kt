package com.thekeeperofpie.artistalleydatabase.anime.news

import java.util.Date

data class AnimeNewsArticleEntry<Category>(
    val id: String,
    val type: AnimeNewsType,
    val icon: String?,
    val image: String?,
    val title: String?,
    val description: String?,
    val link: String?,
    val copyright: String?,
    val date: Date,
    val categories: List<Category>,
)
