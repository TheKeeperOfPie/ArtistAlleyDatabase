package com.thekeeperofpie.artistalleydatabase.cds.search

import kotlinx.serialization.Serializable

@Serializable
data class CdSearchQuery(
    val catalogId: String? = null,
    val titles: List<String> = emptyList(),
    val performers: List<String> = emptyList(),
    val performersById: List<String> = emptyList(),
    val composers: List<String> = emptyList(),
    val composersById: List<String> = emptyList(),
    val series: List<String> = emptyList(),
    val seriesById: List<String> = emptyList(),
    val characters: List<String> = emptyList(),
    val charactersById: List<String> = emptyList(),
    val discs: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val notes: String? = null,
    val catalogIdLocked: Boolean? = null,
    val titlesLocked: Boolean? = null,
    val performersLocked: Boolean? = null,
    val composersLocked: Boolean? = null,
    val seriesLocked: Boolean? = null,
    val charactersLocked: Boolean? = null,
    val discsLocked: Boolean? = null,
    val tagsLocked: Boolean? = null,
    val priceLocked: Boolean? = null,
    val notesLocked: Boolean? = null,
)
